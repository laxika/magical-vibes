import assert from 'node:assert/strict';
import { Readable, Writable } from 'node:stream';
import test from 'node:test';
import { RulesInfoError } from '../rules-cache.mjs';
import { createRequestHandler, runStdioServer } from '../server.mjs';

const KNOWN = {
  rule_id: '120.1a',
  verified: true,
  text: "Damage can't be dealt to an object that's not a battle, a creature, or a planeswalker.",
  subrules: [],
  effective_date: 'June 19, 2026',
};

function stubCache() {
  return {
    getRule: async (ruleId) => {
      if (String(ruleId) === '120.1a') return KNOWN;
      throw new RulesInfoError(`Rule ${ruleId} does not exist. Rules that do exist nearby: 120.1a`);
    },
    searchRules: async (query, limit) => ({ query, limit, matches: [{ rule_id: '120.1' }] }),
  };
}

test('MCP handler advertises the verification tools', async () => {
  const handler = createRequestHandler(stubCache());

  const initialized = await handler({
    method: 'initialize',
    params: { protocolVersion: '2025-06-18' },
  });
  assert.equal(initialized.protocolVersion, '2025-06-18');
  assert.deepEqual(initialized.capabilities, { tools: { listChanged: false } });

  const listed = await handler({ method: 'tools/list' });
  assert.deepEqual(listed.tools.map((tool) => tool.name), ['get_rule', 'search_rules']);
});

test('get_rule verifies every requested number and flags the ones that drifted', async () => {
  const handler = createRequestHandler(stubCache());

  const result = await handler({
    method: 'tools/call',
    params: { name: 'get_rule', arguments: { rule_ids: ['120.1a', '120.1b'] } },
  });

  assert.equal(result.isError, false);
  const { rules } = JSON.parse(result.content[0].text);
  assert.deepEqual(rules[0], KNOWN);
  assert.equal(rules[1].verified, false);
  assert.match(rules[1].error, /does not exist/);
});

test('search_rules passes the query through and bad arguments surface as tool errors', async () => {
  const handler = createRequestHandler(stubCache());

  const found = await handler({
    method: 'tools/call',
    params: { name: 'search_rules', arguments: { query: 'deal damage', limit: 3 } },
  });
  assert.deepEqual(JSON.parse(found.content[0].text), {
    query: 'deal damage',
    limit: 3,
    matches: [{ rule_id: '120.1' }],
  });

  const empty = await handler({
    method: 'tools/call',
    params: { name: 'get_rule', arguments: { rule_ids: [] } },
  });
  assert.equal(empty.isError, true);
  assert.match(empty.content[0].text, /non-empty array/);

  const unknown = await handler({
    method: 'tools/call',
    params: { name: 'get_ruling', arguments: {} },
  });
  assert.equal(unknown.isError, true);
  assert.match(unknown.content[0].text, /Unknown tool/);
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

  await runStdioServer({ input, output, handler: createRequestHandler(stubCache()) });
  const messages = stdout
    .trim()
    .split('\n')
    .map((line) => JSON.parse(line));
  assert.deepEqual(messages.map((message) => message.id), [1, 2]);
  assert.equal(messages[1].result.tools[0].name, 'get_rule');
});
