import assert from 'node:assert/strict';
import fs from 'node:fs';
import test from 'node:test';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const source = fs.readFileSync(fileURLToPath(new URL('../billwise (2).html', import.meta.url)), 'utf8');
const manifest = fs.readFileSync(path.resolve(fileURLToPath(new URL('..', import.meta.url)), 'android/app/src/main/AndroidManifest.xml'), 'utf8');

function applyPayment(bill, amount, date) {
  assert.ok(amount > 0, 'payment amount must be positive');
  assert.ok(amount <= bill.balance, 'payment amount cannot exceed the balance');
  assert.match(date, /^\d{4}-\d{2}-\d{2}$/, 'payment date must be YYYY-MM-DD');
  const payments = Array.isArray(bill.payments) ? bill.payments : [];
  payments.push({ amount, date });
  const advance = payments.reduce((sum, payment) => sum + payment.amount, 0);
  return { ...bill, payments, advance, balance: Math.max(0, bill.total - advance) };
}

test('payment dialog supports amount and custom date', () => {
  assert.match(source, /id="paymentAmount" type="number"/);
  assert.match(source, /id="paymentDate" type="date"/);
  assert.match(source, /onsubmit="receivePayment\(event\)"/);
});

test('partial payment reduces the bill balance', () => {
  const bill = applyPayment({ total: 101, balance: 101, advance: 0 }, 50, '2026-08-01');
  assert.equal(bill.advance, 50);
  assert.equal(bill.balance, 51);
  assert.deepEqual(bill.payments, [{ amount: 50, date: '2026-08-01' }]);
});

test('full payment closes the bill without overpayment', () => {
  const bill = applyPayment({ total: 101, balance: 51, advance: 50, payments: [{ amount: 50, date: '2026-08-01' }] }, 51, '2026-08-24');
  assert.equal(bill.advance, 101);
  assert.equal(bill.balance, 0);
});

test('customer actions are wired to bill creation and filtered history', () => {
  assert.match(source, /onclick="startBillForCustomer\(\$\{c\.id\}\)"/);
  assert.match(source, /onclick="viewCustomerBills\(\$\{c\.id\}\)"/);
  assert.match(source, /function startBillForCustomer\(customerId\)/);
  assert.match(source, /function viewCustomerBills\(customerId\)/);
});

test('share fallback does not use unsupported prompt dialogs', () => {
  assert.doesNotMatch(source, /window\.prompt\(/);
  assert.match(source, /document\.execCommand\('copy'\)/);
});

test('custom total multiplier remains available', () => {
  assert.match(source, /id="totalMultiplier" type="number"/);
  assert.match(source, /\(subtotal \+ tax\) \* multiplier/);
});

test('New Bill has no item photo control', () => {
  assert.doesNotMatch(source, /className = 'item-photo'/);
  assert.doesNotMatch(source, /textContent = 'Photo'/);
});

test('home photo button opens a camera or upload picker', () => {
  assert.match(source, /onclick="openHomePhotoPicker\(\)"/);
  assert.match(source, /id="homePhotoInput" type="file" accept="image\/\*" capture="environment"/);
  assert.match(source, /function handleHomePhoto\(event\)/);
  assert.match(manifest, /android\.permission\.CAMERA/);
});

test('photo upload has OCR support and product parsing', () => {
  assert.match(source, /tesseract\.js@5/);
  assert.match(source, /Tesseract\.recognize/);
  assert.match(source, /function parseOcrItems\(text\)/);
  assert.match(source, /product, quantity, and price/);
});