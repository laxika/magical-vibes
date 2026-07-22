#!/usr/bin/env node

import { fileURLToPath } from 'node:url';
import { CardInfoError, ScryfallSetCache } from './card-cache.mjs';

const SERVER_NAME = 'magical-vibes-card-info';
const SERVER_VERSION = '1.0.0';
const LATEST_PROTOCOL_VERSION = '2025-11-25';
const SUPPORTED_PROTOCOL_VERSIONS = new Set([
  '2024-11-05',
  '2025-03-26',
  '2025-06-18',
  LATEST_PROTOCOL_VERSION,
]);

const GET_CARD_TOOL = {
  name: 'get_card',
  title: 'Get compact Scryfall card data',
  description:
    'Get implementation-relevant Scryfall data for one printing.',
  inputSchema: {
    type: 'object',
    properties: {
      set_code: {
        type: 'string',
        description: 'Scryfall set code, for example DKA or SOS.',
      },
      collector_number: {
        type: ['string', 'number'],
        description: 'Collector number exactly as printed by Scryfall.',
      },
    },
    required: ['set_code', 'collector_number'],
    additionalProperties: false,
  },
  annotations: {
    readOnlyHint: true,
    destructiveHint: false,
    idempotentHint: true,
    openWorldHint: true,
  },
};

export function createRequestHandler(cache = new ScryfallSetCache()) {
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
          title: 'Magical Vibes Card Info',
          version: SERVER_VERSION,
        },
        instructions:
          'Use get_card for card implementation and review. It returns only useful oracle fields; do not fetch raw Scryfall card JSON.',
      };
    }

    if (method === 'ping') return {};
    if (method === 'tools/list') return { tools: [GET_CARD_TOOL] };
    if (method === 'resources/list') return { resources: [] };
    if (method === 'resources/templates/list') return { resourceTemplates: [] };
    if (method === 'prompts/list') return { prompts: [] };

    if (method === 'tools/call') {
      if (params.name !== GET_CARD_TOOL.name) {
        return toolError(`Unknown tool: ${params.name ?? '(missing)'}`);
      }
      const args = params.arguments ?? {};
      try {
        const card = await cache.getCard(args.set_code, args.collector_number);
        return {
          content: [{ type: 'text', text: JSON.stringify(card) }],
          isError: false,
        };
      } catch (error) {
        if (error instanceof CardInfoError) return toolError(error.message);
        throw error;
      }
    }

    throw Object.assign(new Error(`Method not found: ${method}`), { code: -32601 });
  };
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
  const [command, setCode, collectorNumber] = arguments_;
  const cache = new ScryfallSetCache();

  if (command === 'get-card' && setCode && collectorNumber) {
    console.log(JSON.stringify(await cache.getCard(setCode, collectorNumber)));
    return;
  }
  if ((command === 'get-set' || command === 'cache-set') && setCode) {
    const set = await cache.getSet(setCode);
    console.log(
      JSON.stringify(
        command === 'get-set'
          ? set
          : { set: set.set, cards: set.cards.length, downloaded_at: set.downloaded_at },
      ),
    );
    return;
  }
  throw new CardInfoError(
    'Usage: server.mjs [get-card <set> <collector> | get-set <set> | cache-set <set>]',
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
