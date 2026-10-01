import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { mkdir, mkdtemp, rm } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';

const cardModule = new URL('../card-cache.mjs', import.meta.url).href;
const rulesModule = new URL('../../rules-info/rules-cache.mjs', import.meta.url).href;
const script = `
  import { ScryfallSetCache } from ${JSON.stringify(cardModule)};
  import { ComprehensiveRulesCache } from ${JSON.stringify(rulesModule)};
  console.log(JSON.stringify({
    card: new ScryfallSetCache().cacheDir,
    rules: new ComprehensiveRulesCache().cacheDir,
    explicitCard: new ScryfallSetCache({ cacheDir: 'explicit-card-cache' }).cacheDir,
    explicitRules: new ComprehensiveRulesCache({ cacheDir: 'explicit-rules-cache' }).cacheDir,
  }));
`;

function inspectCacheDirectories(cwd, env) {
  return JSON.parse(execFileSync(process.execPath, ['--input-type=module', '-e', script], {
    cwd, env, encoding: 'utf8', windowsHide: true,
  }));
}

test('separate MCP processes and checkout directories resolve the same user cache', async (t) => {
  const root = await mkdtemp(path.join(os.tmpdir(), 'mcp-shared-cache-test-'));
  t.after(() => rm(root, { recursive: true, force: true }));
  const env = { ...process.env };
  delete env.CARD_INFO_CACHE_DIR;
  delete env.RULES_INFO_CACHE_DIR;
  const expected = path.join(os.homedir(), '.magical-vibes-mcp', 'cache');
  for (const name of ['checkout-one', 'checkout-two']) {
    const cwd = path.join(root, name);
    await mkdir(cwd);
    const result = inspectCacheDirectories(cwd, env);
    assert.equal(result.card, expected);
    assert.equal(result.rules, expected);
    assert.equal(result.explicitCard, 'explicit-card-cache');
    assert.equal(result.explicitRules, 'explicit-rules-cache');
  }
});

test('per-server environment overrides remain supported and explicit options take precedence', () => {
  const result = inspectCacheDirectories(os.tmpdir(), {
    ...process.env,
    CARD_INFO_CACHE_DIR: 'custom-card-cache',
    RULES_INFO_CACHE_DIR: 'custom-rules-cache',
  });
  assert.equal(result.card, 'custom-card-cache');
  assert.equal(result.rules, 'custom-rules-cache');
  assert.equal(result.explicitCard, 'explicit-card-cache');
  assert.equal(result.explicitRules, 'explicit-rules-cache');
});
