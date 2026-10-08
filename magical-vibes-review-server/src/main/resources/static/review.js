'use strict';

const content = document.getElementById('content');
const notice = document.getElementById('notice');
const dialog = document.getElementById('new-run');
const confirmBox = document.getElementById('confirm');
const toastBox = document.getElementById('toast');
const refreshToggle = document.getElementById('refresh-toggle');
const STATUS = {CREATED: 'Queued', RUNNING: 'In progress', COMPLETED: 'Done', FAILED: 'Failed'};
const PUBLICATION = {NOT_REQUIRED: 'No test changes', PUSHED: 'Pushed', FAILED: 'Push failed'};
const STUCK_AFTER_MS = 2 * 60 * 60 * 1000;
let generation = 0;
let lastPath = null;
let toastTimer = null;
let live = true;
try { live = localStorage.getItem('review-live') !== 'false'; } catch { /* Storage unavailable. */ }

function node(tag, attrs = {}, ...children) {
  const element = document.createElement(tag);
  for (const [key, value] of Object.entries(attrs)) {
    if (value === null || value === undefined || value === false) continue;
    if (key.startsWith('on')) element.addEventListener(key.slice(2), value);
    else if (key === 'class') element.className = value;
    else element.setAttribute(key, value === true ? '' : value);
  }
  for (const child of children.flat()) {
    if (child !== null && child !== undefined && child !== false) element.append(child instanceof Node ? child : document.createTextNode(String(child)));
  }
  return element;
}

async function api(path, method = 'GET', body) {
  const response = await fetch(`/api${path}`, {method, headers: body === undefined ? {} : {'Content-Type': 'application/json'}, body: body === undefined ? undefined : JSON.stringify(body)});
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try { const result = await response.json(); message = result.error || result.detail || message; } catch { /* Empty error response. */ }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

function fail(error) { document.getElementById('notice-text').textContent = error.message; notice.hidden = false; }
function toast(text) {
  toastBox.textContent = text;
  toastBox.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toastBox.classList.remove('show'), 3500);
}
function confirmDialog(title, body, confirmLabel, danger = false) {
  document.getElementById('confirm-title').textContent = title;
  document.getElementById('confirm-body').replaceChildren(...[body].flat().map(text => text instanceof Node ? text : node('p', {}, text)));
  const ok = document.getElementById('confirm-ok');
  ok.textContent = confirmLabel;
  ok.className = danger ? 'danger' : 'primary';
  confirmBox.returnValue = 'cancel';
  confirmBox.showModal();
  return new Promise(resolve => confirmBox.addEventListener('close', () => resolve(confirmBox.returnValue === 'ok'), {once: true}));
}
/** Button that runs {@code handler}; a string result is shown as a toast and {@code false} means the user cancelled. */
function action(label, handler, cls = '', attrs = {}) {
  return node('button', {type: 'button', class: cls, ...attrs, onclick: async event => {
    const button = event.currentTarget;
    button.disabled = true;
    try {
      const result = await handler();
      if (result === false) return;
      notice.hidden = true;
      if (typeof result === 'string') toast(result);
      await render();
    } catch (error) { fail(error); }
    finally { button.disabled = false; }
  }}, label);
}
function link(text, route, cls = '') { return node('a', {href: `#${route}`, class: cls}, text); }
function badge(text, cls = text.toLowerCase()) { return node('span', {class: `badge ${cls}`}, text); }
function statusBadge(status) { return badge(STATUS[status] || status, status.toLowerCase()); }
function outcomeBadge(outcome, findingCount) {
  if (outcome === 'PASS') return badge('Clean', 'pass');
  if (outcome === 'FINDINGS') return badge(findingCount == null ? 'Findings' : plural(findingCount, 'finding'), 'findings');
  if (outcome === 'ERROR') return badge('Error', 'error');
  return null;
}
function plural(count, word) { return `${Number(count).toLocaleString()} ${word}${count === 1 ? '' : 's'}`; }
function heading(title, subtitle, ...controls) { return node('div', {class: 'page-heading'}, node('div', {}, node('h1', {}, title), subtitle ? node('p', {class: 'intro'}, subtitle) : null), node('div', {class: 'controls'}, controls)); }
function sectionHeading(title, ...extra) { return node('div', {class: 'section-heading'}, node('h2', {}, title), extra); }
function table(headers, rows) { return node('div', {class: 'panel table-wrap'}, node('table', {}, node('thead', {}, node('tr', {}, headers.map(text => node('th', {}, text)))), node('tbody', {}, rows.map(cells => node('tr', {}, cells.map(cell => node('td', {}, cell))))))); }
function empty(text, ...extra) { return node('div', {class: 'panel empty'}, node('p', {}, text), extra); }
function date(value) { return value ? new Date(value).toLocaleString() : '—'; }
function duration(ms) {
  const minutes = Math.max(0, Math.floor(ms / 60000));
  if (minutes < 1) return 'under a minute';
  if (minutes < 60) return `${minutes} min`;
  const hours = Math.floor(minutes / 60);
  if (hours < 48) return `${hours} h ${minutes % 60} min`;
  return `${Math.floor(hours / 24)} days`;
}
function relative(value) {
  if (!value) return '—';
  const ms = Date.now() - new Date(value).getTime();
  return node('time', {datetime: value, title: date(value)}, ms < 60000 ? 'just now' : `${duration(ms)} ago`);
}
function commit(hash) {
  if (!hash) return '—';
  return node('button', {type: 'button', class: 'hash', title: `${hash} — click to copy`, onclick: async () => {
    try { await navigator.clipboard.writeText(hash); toast('Commit hash copied'); } catch { toast(hash); }
  }}, hash.slice(0, 7));
}
function cost(value) { return value == null ? 'Unavailable' : new Intl.NumberFormat('en-US', {style: 'currency', currency: 'USD', minimumFractionDigits: 2, maximumFractionDigits: 6}).format(value); }
function money(value) { return new Intl.NumberFormat('en-US', {style: 'currency', currency: 'USD', minimumFractionDigits: 2, maximumFractionDigits: value < 0.1 ? 4 : 2}).format(value); }
function sumCosts(items) {
  const priced = items.filter(item => item.estimatedCostUsd != null);
  return {
    estimatedCostUsd: priced.length ? priced.reduce((total, item) => total + item.estimatedCostUsd, 0) : null,
    missingCostCount: items.reduce((total, item) => total + (item.missingCostCount ?? (item.finishedAt && item.estimatedCostUsd == null ? 1 : 0)), 0)
  };
}
function perCard(summary) {
  if (summary.pricedCards == null) return null;
  if (!summary.pricedCards || summary.estimatedCostUsd == null) return node('span', {class: 'subline'}, 'No per-card cost yet');
  return node('span', {class: 'subline per-card', title: `Average over ${plural(summary.pricedCards, 'card')} with cost data, including retries.`},
    `≈ ${money(summary.estimatedCostUsd / summary.pricedCards)} per card`);
}
function costSummary(summary) {
  return node('span', {class: 'cost', title: 'Estimated USD token cost, including previous attempts; actual billing may differ.'},
    summary.estimatedCostUsd == null ? 'Unavailable' : money(summary.estimatedCostUsd), summary.estimatedCostUsd != null && summary.missingCostCount ? ' (partial)' : null,
    perCard(summary),
    summary.missingCostCount ? node('span', {class: 'subline'}, `${plural(summary.missingCostCount, 'finished attempt')} without cost data`) : null);
}
function totalCost(label, summary) { return node('p', {class: 'cost-total'}, node('span', {}, label), costSummary(summary)); }
function route() { const [path, query] = location.hash.slice(1).split('?'); return {path: path || 'runs', params: new URLSearchParams(query)}; }
function withParams(path, params, changes) {
  const next = new URLSearchParams(params);
  for (const [key, value] of Object.entries(changes)) { if (value) next.set(key, value); else next.delete(key); }
  const query = next.toString();
  return query ? `${path}?${query}` : path;
}
function pagination(result, path, params) {
  const totalPages = Math.max(1, Math.ceil(result.total / result.pageSize));
  if (totalPages === 1) return node('div', {class: 'pagination'}, plural(result.total, 'item'));
  return node('div', {class: 'pagination'},
    result.page > 0 ? link('← Previous', withParams(path, params, {page: String(result.page - 1)}), 'page-link') : null,
    `${plural(result.total, 'item')} · Page ${result.page + 1} of ${totalPages}`,
    result.page + 1 < totalPages ? link('Next →', withParams(path, params, {page: String(result.page + 1)}), 'page-link') : null);
}

function progress(run, large = false) {
  const total = run.total || 0;
  const share = count => total ? `${count / total * 100}%` : '0%';
  const percent = total ? Math.floor(run.completed / total * 100) : 0;
  const legend = [['done', 'Done', run.completed], ['failed', 'Failed', run.failed], ['running', 'In progress', run.running], ['queued', 'Queued', run.created]];
  return node('div', {class: `progress${large ? ' large' : ''}`},
    node('div', {class: 'progress-head'}, node('span', {class: 'count'}, `${run.completed.toLocaleString()} of ${total.toLocaleString()} done`), node('span', {class: 'percent'}, `${percent}%`)),
    node('div', {class: 'track', role: 'img', 'aria-label': legend.map(([, label, count]) => `${count} ${label.toLowerCase()}`).join(', ')},
      legend.slice(0, 3).map(([cls, label, count]) => count ? node('span', {class: `seg ${cls}`, style: `width:${share(count)}`, title: `${label}: ${count}`}) : null)),
    node('div', {class: 'legend'}, legend.filter(([cls, , count]) => count || cls === 'done' || cls === 'queued').map(([cls, label, count]) => node('span', {}, node('i', {class: `dot ${cls}`}), `${label} ${count.toLocaleString()}`))));
}

function activeHero(active) {
  if (!active) {
    return node('section', {class: 'panel hero idle'}, node('div', {},
      node('p', {class: 'eyebrow'}, 'Workers are idle'),
      node('h2', {}, 'New claims are paused'),
      node('p', {class: 'muted'}, 'Running workers keep polling. Make a run active below to put them back to work.')));
  }
  return node('section', {class: 'panel hero'},
    node('div', {class: 'hero-main'},
      node('p', {class: 'eyebrow'}, node('span', {class: 'live-dot on'}), 'Workers are reviewing'),
      node('h2', {}, link(active.name, `runs/${active.id}`)),
      node('p', {class: 'muted'}, `${active.model} · ${active.reasoningEffort}`),
      progress(active, true)),
    node('div', {class: 'hero-side'},
      node('dl', {class: 'hero-stats'},
        node('div', {}, node('dt', {}, 'In progress'), node('dd', {}, active.running)),
        node('div', {}, node('dt', {}, 'Findings'), node('dd', {}, active.findingCount, node('span', {class: 'subline'}, `in ${plural(active.cardsWithFindings, 'card')}`))),
        node('div', {}, node('dt', {}, 'Failed'), node('dd', {class: active.failed ? 'danger-text' : ''}, active.failed)),
        node('div', {}, node('dt', {}, 'Estimated cost'), node('dd', {}, costSummary(active)))),
      node('div', {class: 'controls'}, link('Open run', `runs/${active.id}`, 'button-link primary'), action('Pause new claims', () => api('/active-run', 'PUT', {runId: null}).then(() => 'New claims paused')))));
}

async function home() {
  const data = await api('/overview');
  const active = data.runs.find(run => run.active);
  const create = action('Create run', () => { dialog.showModal(); return false; }, 'primary');
  const result = [heading('Review runs', 'Compare what each model finds across the card catalog.', create)];
  if (!data.runs.length) {
    result.push(empty('No runs yet. Create your first run to queue every implemented card — reprints share one review.', action('Create your first run', () => { dialog.showModal(); return false; }, 'primary')));
    return result;
  }
  result.push(activeHero(active), totalCost('Total estimated cost across runs', sumCosts(data.runs)));
  result.push(sectionHeading('All runs', node('span', {class: 'muted'}, plural(data.runs.length, 'run'))));
  result.push(table(['Run', 'Model / level', 'Progress', 'Findings', 'Estimated cost', ''], data.runs.map(run => [
    node('div', {}, link(run.name, `runs/${run.id}`, 'run-title'), run.active ? ' ' : null, run.active ? badge('Active') : null, node('span', {class: 'subline'}, 'Created ', relative(run.createdAt))),
    node('div', {}, run.model, node('span', {class: 'subline'}, run.reasoningEffort)), progress(run),
    node('div', {}, node('span', {class: 'number'}, run.findingCount), node('span', {class: 'subline'}, `in ${plural(run.cardsWithFindings, 'card')}`)), costSummary(run),
    run.active ? null : action('Make active', () => api('/active-run', 'PUT', {runId: run.id}).then(() => `${run.name} is now active`))
  ])));
  if (data.models.length) {
    result.push(sectionHeading('Findings by model', node('span', {class: 'muted'}, 'Totals across runs; equivalent bugs are counted separately.')));
    result.push(table(['Model', 'Reasoning level', 'Runs', 'Cards reviewed', 'Findings', 'Cards with findings'], data.models.map(model => [model.model, model.reasoningEffort, model.runs, model.completed.toLocaleString(), node('span', {class: 'number'}, model.findingCount), model.cardsWithFindings])));
  }
  return result;
}

function searchForm(path, params, placeholder) {
  const form = node('form', {class: 'controls search-form', onsubmit: event => {
    event.preventDefault();
    location.hash = withParams(path, params, {query: new FormData(form).get('query').trim(), page: null});
  }});
  form.append(node('input', {name: 'query', value: params.get('query') || '', placeholder, 'aria-label': placeholder, type: 'search'}), node('button', {type: 'submit'}, 'Filter'));
  if (params.get('query')) form.append(link('Clear', withParams(path, params, {query: null, page: null}), 'clear-link'));
  return form;
}

function statusChips(path, params, run) {
  const current = params.get('status') || '';
  const chips = [['', 'All', run.total], ['CREATED', STATUS.CREATED, run.created], ['RUNNING', STATUS.RUNNING, run.running], ['COMPLETED', STATUS.COMPLETED, run.completed], ['FAILED', STATUS.FAILED, run.failed]];
  return node('nav', {class: 'chips', 'aria-label': 'Filter by status'}, chips.map(([status, label, count]) =>
    node('a', {href: `#${withParams(path, params, {status, page: null})}`, class: `chip ${status.toLowerCase()}`, 'aria-current': status === current ? 'page' : null}, label, node('span', {class: 'chip-count'}, count.toLocaleString()))));
}

function tile(label, value, sub, href, cls = '') {
  const body = [node('span', {class: 'tile-label'}, label), node('span', {class: 'tile-value'}, value), sub ? node('span', {class: 'tile-sub'}, sub) : null];
  return href ? node('a', {class: `tile ${cls}`, href: `#${href}`}, body) : node('div', {class: `tile ${cls}`}, body);
}

function runningPanel(running, run) {
  if (!run.running) return null;
  const now = Date.now();
  const rows = running.items.map(task => {
    const elapsed = task.startedAt ? now - new Date(task.startedAt).getTime() : 0;
    const stuck = elapsed > STUCK_AFTER_MS;
    return node('li', {class: stuck ? 'stuck' : ''},
      node('div', {class: 'running-card'}, link(task.cardName, `cards/${task.cardId}`, 'card-name small'), node('span', {class: 'subline'}, `${task.setCode} ${task.collectorNumber}`)),
      node('div', {class: 'running-worker'}, node('span', {class: 'live-dot on'}), task.workerId || 'unknown worker'),
      node('div', {class: 'running-time', title: `Started ${date(task.startedAt)}`}, duration(elapsed), stuck ? node('span', {class: 'subline danger-text'}, 'Possibly stuck — past the 2 h worker timeout') : null));
  });
  return [sectionHeading('In progress now', node('span', {class: 'muted'}, `${plural(run.running, 'task')} claimed`)),
    node('ul', {class: 'panel running-list'}, rows),
    running.total > running.items.length ? node('p', {class: 'muted'}, `Showing ${running.items.length} of ${running.total}.`) : null];
}

function recoveryPanel(run) {
  const unfinished = run.running + run.failed;
  const resetAll = action(`Reset ${unfinished.toLocaleString()} unfinished to Queued`, async () => {
    const ok = await confirmDialog('Reset unfinished tasks?', [
      `This sends ${plural(unfinished, 'task')} (${run.running.toLocaleString()} in progress, ${run.failed.toLocaleString()} failed) back to Queued.`,
      node('p', {class: 'callout'}, 'Stop all workers first. Results that their current attempts submit later will be rejected. Restart the workers afterwards to finish these cards.'),
    ], 'Reset to Queued', true);
    if (!ok) return false;
    const result = await api(`/runs/${run.id}/requeue`, 'POST', {status: 'UNFINISHED'});
    return `Reset ${plural(result.requeued, 'task')} to Queued`;
  }, 'danger', {disabled: unfinished === 0});
  const resetFailed = action('Requeue failed only', async () => {
    const result = await api(`/runs/${run.id}/requeue`, 'POST', {status: 'FAILED'});
    return `Requeued ${plural(result.requeued, 'failed task')}`;
  }, '', {disabled: run.failed === 0});
  const resetRunning = action('Requeue in progress only', async () => {
    const ok = await confirmDialog('Requeue in-progress tasks?', `This sends ${plural(run.running, 'task')} back to Queued. Results from their current workers will be rejected.`, 'Requeue', true);
    if (!ok) return false;
    const result = await api(`/runs/${run.id}/requeue`, 'POST', {status: 'RUNNING'});
    return `Requeued ${plural(result.requeued, 'in-progress task')}`;
  }, '', {disabled: run.running === 0});
  return node('section', {class: 'panel recovery'},
    node('div', {}, node('h3', {}, 'Recover unfinished work'),
      node('p', {class: 'muted'}, unfinished ? 'Stop workers, reset, then restart workers to pick up the remaining cards.' : 'Nothing to recover — no failed or in-progress tasks.')),
    node('div', {class: 'controls'}, resetFailed, resetRunning, resetAll));
}

function publication(task) {
  if (!task.publicationStatus) return node('span', {class: 'muted'}, '—');
  if (task.publicationStatus === 'PUSHED') return node('div', {}, PUBLICATION.PUSHED, ' ', commit(task.publishedCommit));
  if (task.publicationStatus === 'FAILED') return node('div', {}, badge(PUBLICATION.FAILED, 'failed'), task.publicationError ? node('span', {class: 'subline error-line', title: task.publicationError}, task.publicationError) : null);
  return node('span', {class: 'muted'}, PUBLICATION[task.publicationStatus] || task.publicationStatus);
}

async function runPage(id, params) {
  const [run, tasks] = await Promise.all([api(`/runs/${id}`), api(`/runs/${id}/tasks?${params}`)]);
  const running = run.running ? await api(`/runs/${id}/tasks?status=RUNNING`) : null;
  const path = `runs/${id}`;
  const controls = run.active
    ? [badge('Active'), action('Pause new claims', () => api('/active-run', 'PUT', {runId: null}).then(() => 'New claims paused'))]
    : [action('Make active', () => api('/active-run', 'PUT', {runId: run.id}).then(() => `${run.name} is now active`), 'primary')];
  const percent = run.total ? Math.floor(run.completed / run.total * 100) : 0;
  const result = [link('← All runs', 'runs', 'back-link'),
    heading(run.name, node('span', {}, `${run.model} · ${run.reasoningEffort} · created `, relative(run.createdAt), ' · catalog ', commit(run.catalogCommit)), controls),
    node('div', {class: 'panel progress-panel'}, progress(run, true)),
    node('div', {class: 'tiles'},
      tile('Done', run.completed.toLocaleString(), `${percent}% of ${run.total.toLocaleString()}`, withParams(path, new URLSearchParams(), {status: 'COMPLETED'}), 'done'),
      tile('Findings', run.findingCount.toLocaleString(), `in ${plural(run.cardsWithFindings, 'card')}`, null, 'findings'),
      tile('In progress', run.running.toLocaleString(), run.running ? 'claimed by workers' : 'no active claims', withParams(path, new URLSearchParams(), {status: 'RUNNING'}), 'running'),
      tile('Failed', run.failed.toLocaleString(), run.publicationFailures ? `${plural(run.publicationFailures, 'push failure')} too` : 'execution failures', withParams(path, new URLSearchParams(), {status: 'FAILED'}), run.failed ? 'failed' : ''),
      tile('Queued', run.created.toLocaleString(), 'waiting for a worker', withParams(path, new URLSearchParams(), {status: 'CREATED'}), 'queued'),
      tile('Estimated cost', run.estimatedCostUsd == null ? '—' : money(run.estimatedCostUsd),
        node('span', {}, run.pricedCards && run.estimatedCostUsd != null ? `≈ ${money(run.estimatedCostUsd / run.pricedCards)} per card` : 'no cost data yet',
          run.missingCostCount ? node('span', {class: 'subline'}, `${plural(run.missingCostCount, 'attempt')} without cost data`) : null), null, 'cost')),
    runningPanel(running, run),
    recoveryPanel(run),
    sectionHeading('Cards'),
    node('div', {class: 'filter-bar'}, statusChips(path, params, run), searchForm(path, params, 'Filter cards…'))];
  result.push(tasks.items.length ? table(['Card', 'Printing', 'Review', 'Worker', 'Estimated cost', 'Publication', ''], tasks.items.map(task => [
    node('div', {}, link(task.cardName, `cards/${task.cardId}`, 'card-name'), node('span', {class: 'subline'}, task.className.split('.').pop())),
    node('span', {class: 'printing'}, `${task.setCode} ${task.collectorNumber}`),
    node('div', {}, node('div', {class: 'badges'}, statusBadge(task.status), outcomeBadge(task.outcome, task.findingCount)),
      task.executionError ? node('span', {class: 'subline error-line', title: task.executionError}, task.executionError) : null),
    node('div', {}, task.workerId || node('span', {class: 'muted'}, '—'), task.startedAt ? node('span', {class: 'subline'}, task.status === 'RUNNING' ? 'started ' : '', relative(task.startedAt)) : null),
    task.estimatedCostUsd == null ? node('span', {class: 'muted'}, '—') : cost(task.estimatedCostUsd), publication(task),
    task.status === 'CREATED' ? null : action('Requeue', async () => {
      if (task.status === 'RUNNING' && !await confirmDialog('Requeue this task?', `${task.cardName} is in progress on ${task.workerId}. Its current result will be rejected.`, 'Requeue', true)) return false;
      await api(`/tasks/${task.id}/requeue`, 'POST');
      return `${task.cardName} requeued`;
    }, 'small')
  ])) : empty('No cards match these filters.'));
  result.push(pagination(tasks, path, params));
  return result;
}

async function catalogPage(params) {
  const cards = await api(`/cards?${params}`);
  return [heading('Card catalog', 'Find a card to compare its reviews across models and runs.'), node('div', {class: 'filter-bar'}, searchForm('cards', params, 'Name, class, or printing…')),
    cards.items.length ? table(['Card', 'Implementation'], cards.items.map(card => [link(card.cardName, `cards/${card.id}`, 'card-name'), node('code', {}, card.className)])) : empty('No cards found. Create a run to populate the catalog, or try another search.'),
    pagination(cards, 'cards', params)];
}

function attemptBody(attempt) {
  const took = attempt.startedAt && attempt.finishedAt ? duration(new Date(attempt.finishedAt) - new Date(attempt.startedAt)) : null;
  const meta = [['Worker', attempt.workerId], ['Started', relative(attempt.startedAt)], ['Finished', attempt.finishedAt ? relative(attempt.finishedAt) : 'not yet'], ['Took', took],
    ['Reviewed commit', commit(attempt.reviewedCommit)], ['Publication', attempt.publicationStatus ? PUBLICATION[attempt.publicationStatus] : 'pending'], ['Published commit', attempt.publishedCommit ? commit(attempt.publishedCommit) : null],
    ['Estimated cost', cost(attempt.estimatedCostUsd)], ['Tokens', attempt.inputTokens == null ? null : `${attempt.inputTokens.toLocaleString()} in (${attempt.cachedInputTokens.toLocaleString()} cached) · ${attempt.outputTokens.toLocaleString()} out`]];
  return [attempt.findings.length ? node('ol', {class: 'findings'}, attempt.findings.map(description => node('li', {}, description))) : node('p', {class: 'muted'}, attempt.outcome === 'PASS' ? 'No bugs reported.' : attempt.outcome === 'ERROR' ? 'The review did not complete.' : 'Waiting for the worker to submit its result.'),
    attempt.executionError ? node('p', {class: 'failure'}, attempt.executionError) : null,
    attempt.publicationError ? node('p', {class: 'failure'}, `Publication failed: ${attempt.publicationError}`) : null,
    node('dl', {class: 'review-meta'}, meta.filter(([, value]) => value != null).map(([label, value]) => node('div', {}, node('dt', {}, label), node('dd', {}, value))))];
}

async function cardPage(id) {
  const card = await api(`/cards/${id}`);
  return [link('← Card catalog', 'cards', 'back-link'), heading(card.cardName, card.className),
    node('div', {class: 'badges printings'}, card.printings.map(printing => node('span', {class: 'printing'}, `${printing.setCode} ${printing.collectorNumber}`))),
    totalCost('Total estimated cost for this card', sumCosts(card.reviews.flatMap(review => review.attempts))),
    card.reviews.length ? null : empty('This card has not been part of any run yet.'),
    node('div', {class: 'comparison'}, card.reviews.map(review => {
      const current = review.attempts.find(attempt => attempt.current);
      const history = review.attempts.filter(attempt => !attempt.current);
      return node('article', {class: 'review'}, node('div', {class: 'review-heading'}, node('div', {}, node('h3', {}, link(review.run.name, `runs/${review.run.id}`)), node('p', {class: 'muted'}, `${review.run.model} · ${review.run.reasoningEffort}`)),
        node('div', {class: 'badges'}, statusBadge(review.status), current ? outcomeBadge(current.outcome, current.findings.length) : null)),
        totalCost('Estimated review cost', sumCosts(review.attempts)),
        current ? attemptBody(current) : node('p', {class: 'muted'}, 'This card is waiting to be claimed.'),
        history.length ? node('details', {'data-key': `history-${review.id}`}, node('summary', {}, `${plural(history.length, 'previous attempt')}`), history.map(attempt => node('div', {class: 'history-item'}, outcomeBadge(attempt.outcome, attempt.findings.length) || badge('Requeued', 'created'), attemptBody(attempt)))) : null,
        review.status === 'CREATED' ? null : action('Requeue review', async () => {
          if (review.status === 'RUNNING' && !await confirmDialog('Requeue this review?', 'It is in progress. Its current result will be rejected.', 'Requeue', true)) return false;
          await api(`/tasks/${review.id}/requeue`, 'POST');
          return 'Review requeued';
        }, 'small')
      );
    }))];
}

async function render() {
  const version = ++generation;
  const {path, params} = route();
  document.getElementById('nav-runs').setAttribute('aria-current', path.startsWith('runs') ? 'page' : 'false');
  document.getElementById('nav-cards').setAttribute('aria-current', path.startsWith('cards') ? 'page' : 'false');
  try {
    let children;
    if (/^runs\/\d+$/.test(path)) children = await runPage(path.split('/')[1], params);
    else if (/^cards\/\d+$/.test(path)) children = await cardPage(path.split('/')[1]);
    else if (path === 'cards') children = await catalogPage(params);
    else children = await home();
    if (version !== generation) return;
    const samePage = lastPath === location.hash;
    const scroll = window.scrollY;
    const open = new Set([...content.querySelectorAll('details[open][data-key]')].map(element => element.dataset.key));
    content.replaceChildren(...children.flat().filter(child => child !== null && child !== undefined));
    if (samePage) {
      content.querySelectorAll('details[data-key]').forEach(element => { if (open.has(element.dataset.key)) element.open = true; });
      window.scrollTo(0, scroll);
    }
    lastPath = location.hash;
    updateLiveNote(`Updated ${new Date().toLocaleTimeString()}`);
  } catch (error) { if (version === generation) fail(error); }
}

function updateLiveNote(text) {
  refreshToggle.setAttribute('aria-pressed', String(live));
  refreshToggle.classList.toggle('paused', !live);
  document.getElementById('refresh-note').textContent = live ? text || 'Live' : 'Paused — click to resume';
}

document.getElementById('global-search').addEventListener('submit', event => { event.preventDefault(); location.hash = `cards?${new URLSearchParams(new FormData(event.target))}`; });
document.getElementById('close-dialog').addEventListener('click', () => dialog.close());
document.getElementById('notice-close').addEventListener('click', () => { notice.hidden = true; });
refreshToggle.addEventListener('click', () => {
  live = !live;
  try { localStorage.setItem('review-live', String(live)); } catch { /* Storage unavailable. */ }
  updateLiveNote();
  if (live) render();
});
document.getElementById('run-form').addEventListener('submit', async event => {
  event.preventDefault();
  const submit = event.target.querySelector('[type=submit]');
  submit.disabled = true;
  submit.textContent = 'Creating run…';
  try { const run = await api('/runs', 'POST', Object.fromEntries(new FormData(event.target))); dialog.close(); notice.hidden = true; toast(`Created ${run.name} with ${plural(run.total, 'card')}`); location.hash = `runs/${run.id}`; }
  catch (error) { dialog.close(); fail(error); }
  finally { submit.disabled = false; submit.textContent = 'Create run'; }
});
document.addEventListener('keydown', event => {
  if (event.key !== '/' || ['INPUT', 'SELECT', 'TEXTAREA'].includes(document.activeElement.tagName) || document.querySelector('dialog[open]')) return;
  event.preventDefault();
  document.getElementById('search').focus();
});
window.addEventListener('hashchange', () => { notice.hidden = true; render(); });
setInterval(() => { if (live && !document.hidden && !document.querySelector('dialog[open]') && !['INPUT', 'SELECT', 'TEXTAREA'].includes(document.activeElement.tagName)) render(); }, 5000);
updateLiveNote();
render();
