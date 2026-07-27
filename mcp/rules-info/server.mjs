#!/usr/bin/env node

import { fileURLToPath } from 'node:url';
import { ComprehensiveRulesCache, RulesInfoError } from './rules-cache.mjs';

const SERVER_NAME = 'magical-vibes-rules-info';
const SERVER_VERSION = '1.0.0';
const LATEST_PROTOCOL_VERSION = '2025-11-25';
const SUPPORTED_PROTOCOL_VERSIONS = new Set([
  '2024-11-05',
  '2025-03-26',
  '2025-06-18',
  LATEST_PROTOCOL_VERSION,
]);

const READ_ONLY = {
  readOnlyHint: true,
  destructiveHint: false,
  idempotentHint: true,
  openWorldHint: true,
};

const GET_RULE_TOOL = {
  name: 'get_rule',
  title: 'Get official Comprehensive Rules text',
  description:
    'Return the verbatim Comprehensive Rules text for one or more rule numbers. Use this to verify every rule number before citing it in code or comments — rule numbers drift between releases.',
  inputSchema: {
    type: 'object',
    properties: {
      rule_ids: {
        type: 'array',
        description: 'Rule numbers such as 120.1a, 509.1, or 120.',
        items: { type: 'string' },
        minItems: 1,
        maxItems: 25,
      },
    },
    required: ['rule_ids'],
    additionalProperties: false,
  },
  annotations: READ_ONLY,
};

const SEARCH_RULES_TOOL = {
  name: 'search_rules',
  title: 'Search the Comprehensive Rules',
  description:
    'Find rule numbers whose text contains a phrase. Use this when a rule number failed verification, or when you know the concept but not the number.',
  inputSchema: {
    type: 'object',
    properties: {
      query: { type: 'string', description: 'Phrase to look for, at least 3 characters.' },
      limit: { type: 'integer', minimum: 1, maximum: 50, description: 'Defaults to 10.' },
    },
    required: ['query'],
    additionalProperties: false,
  },
  annotations: READ_ONLY,
};

const TOOLS = [GET_RULE_TOOL, SEARCH_RULES_TOOL];

async function getRules(cache, ruleIds) {
  if (!Array.isArray(ruleIds) || ruleIds.length === 0) {
    throw new RulesInfoError('rule_ids must be a non-empty array of rule numbers');
  }
  const rules = [];
  for (const ruleId of ruleIds) {
    try {
      rules.push(await cache.getRule(ruleId));
    } catch (error) {
      if (!(error instanceof RulesInfoError)) throw error;
      // One bad number must not hide the rules that did verify.
      rules.push({ rule_id: String(ruleId), verified: false, error: error.message });
    }
  }
  return { rules };
}

export function createRequestHandler(cache = new ComprehensiveRulesCache()) {
  return async function handleRequest(message) {
    const method = message?.method;
    const params = message?.params ?? {};

    if (method === 'initialize') {
      const requested = params.protocolVersion;
      return {
        protocolVersion: SUPPORTED_PROTOCOL_VERSIONS.has(requested)
          ? requested
          : LATEST_PROTOCOL_VERSION,
        capabilities: { tools: { listChanged: false } },
        serverInfo: {
          name: SERVER_NAME,
          title: 'Magical Vibes Comprehensive Rules',
          version: SERVER_VERSION,
        },
        instructions:
          'Verify every Comprehensive Rules number with get_rule before citing it in code, comments, or commit messages — rule numbers drift between releases. Use search_rules to find the current number when verification fails.',
      };
    }

    if (method === 'ping') return {};
    if (method === 'tools/list') return { tools: TOOLS };
    if (method === 'resources/list') return { resources: [] };
    if (method === 'resources/templates/list') return { resourceTemplates: [] };
    if (method === 'prompts/list') return { prompts: [] };

    if (method === 'tools/call') {
      const args = params.arguments ?? {};
      try {
        if (params.name === GET_RULE_TOOL.name) {
          return toolResult(await getRules(cache, args.rule_ids));
        }
        if (params.name === SEARCH_RULES_TOOL.name) {
          return toolResult(await cache.searchRules(args.query, args.limit ?? 10));
        }
        return toolError(`Unknown tool: ${params.name ?? '(missing)'}`);
      } catch (error) {
        if (error instanceof RulesInfoError) return toolError(error.message);
        throw error;
      }
    }

    throw Object.assign(new Error(`Method not found: ${method}`), { code: -32601 });
  };
}

function toolResult(payload) {
  return { content: [{ type: 'text', text: JSON.stringify(payload) }], isError: false };
}

function toolError(message) {
  return { content: [{ type: 'text', text: message }], isError: true };
}

function writeMessage(output, message) {
  output.write(`${JSON.stringify(message)}\n`);
}

export async function runStdioServer({
  input = process.stdin,
  output = process.stdout,
  handler = createRequestHandler(),
} = {}) {
  input.setEncoding('utf8');
  let buffer = '';

  for await (const chunk of input) {
    buffer += chunk;
    let newline;
    while ((newline = buffer.indexOf('\n')) >= 0) {
      const line = buffer.slice(0, newline).replace(/\r$/, '');
      buffer = buffer.slice(newline + 1);
      if (line.trim()) await processLine(line, handler, output);
    }
  }
  if (buffer.trim()) await processLine(buffer, handler, output);
}

async function processLine(line, handler, output) {
  let request;
  try {
    request = JSON.parse(line);
  } catch {
    writeMessage(output, {
      jsonrpc: '2.0',
      id: null,
      error: { code: -32700, message: 'Parse error' },
    });
    return;
  }

  if (!Object.hasOwn(request, 'id')) return;

  try {
    const result = await handler(request);
    writeMessage(output, { jsonrpc: '2.0', id: request.id, result });
  } catch (error) {
    writeMessage(output, {
      jsonrpc: '2.0',
      id: request.id,
      error: { code: error.code ?? -32603, message: error.message || 'Internal error' },
    });
  }
}

async function runCli(arguments_) {
  const [command, ...rest] = arguments_;
  const cache = new ComprehensiveRulesCache();

  if (command === 'get-rule' && rest.length > 0) {
    console.log(JSON.stringify(await getRules(cache, rest), null, 2));
    return;
  }
  if (command === 'search' && rest.length > 0) {
    console.log(JSON.stringify(await cache.searchRules(rest.join(' ')), null, 2));
    return;
  }
  if (command === 'refresh' || command === 'status') {
    const document = await cache.getRules({ force: command === 'refresh' });
    console.log(
      JSON.stringify({
        source_url: document.source_url,
        effective_date: document.effective_date,
        downloaded_at: document.downloaded_at,
        rule_count: document.rule_count ?? Object.keys(document.rules).length,
        stale: document.stale ?? false,
      }),
    );
    return;
  }
  throw new RulesInfoError(
    'Usage: server.mjs [get-rule <id...> | search <phrase> | status | refresh]',
  );
}

const isMain = process.argv[1] && fileURLToPath(import.meta.url) === process.argv[1];
if (isMain) {
  const arguments_ = process.argv.slice(2);
  const operation = arguments_.length > 0 ? runCli(arguments_) : runStdioServer();
  operation.catch((error) => {
    console.error(error.message);
    process.exitCode = 1;
  });
}
