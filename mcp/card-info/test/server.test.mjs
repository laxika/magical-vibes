import assert from 'node:assert/strict';
import { Readable, Writable } from 'node:stream';
import test from 'node:test';
import { createRequestHandler, runStdioServer } from '../server.mjs';

test('MCP handler advertises one compact lookup tool and returns compact JSON text', async () => {
  const requestedCard = { name: 'Tragic Slip', set: 'dka', collector_number: '76' };
  const calls = [];
  const handler = createRequestHandler({
    getCard: async (...args) => {
      calls.push(args);
      return requestedCard;
    },
  });

  const initialized = await handler({
    method: 'initialize',
    params: { protocolVersion: '2025-06-18' },
  });
  assert.equal(initialized.protocolVersion, '2025-06-18');
  assert.deepEqual(initialized.capabilities, { tools: { listChanged: false } });

  const listed = await handler({ method: 'tools/list' });
  assert.deepEqual(listed.tools.map((tool) => tool.name), ['get_card']);

  const result = await handler({
    method: 'tools/call',
    params: {
      name: 'get_card',
      arguments: { set_code: 'DKA', collector_number: '76' },
    },
  });
  assert.equal(result.isError, false);
  assert.deepEqual(JSON.parse(result.content[0].text), requestedCard);
  assert.deepEqual(calls[0], ['DKA', '76']);
});

test('stdio server speaks newline-delimited JSON-RPC without extra stdout output', async () => {
  let stdout = '';
  const input = Readable.from([
    [
      JSON.stringify({
        jsonrpc: '2.0',
        id: 1,
        method: 'initialize',
        params: { protocolVersion: '2025-11-25', capabilities: {}, clientInfo: {} },
      }),
      JSON.stringify({ jsonrpc: '2.0', method: 'notifications/initialized' }),
      JSON.stringify({ jsonrpc: '2.0', id: 2, method: 'tools/list', params: {} }),
    ].join('\n') + '\n',
  ]);
  const output = new Writable({
    write(chunk, _encoding, callback) {
      stdout += chunk.toString();
      callback();
    },
  });

  await runStdioServer({ input, output });
  const messages = stdout
    .trim()
    .split('\n')
    .map((line) => JSON.parse(line));
  assert.deepEqual(messages.map((message) => message.id), [1, 2]);
  assert.equal(messages[1].result.tools[0].name, 'get_card');
});
