'use strict';

const content = document.getElementById('content');
const notice = document.getElementById('notice');
const dialog = document.getElementById('new-run');
let generation = 0;

function node(tag, attrs = {}, ...children) {
  const element = document.createElement(tag);
  for (const [key, value] of Object.entries(attrs)) {
    if (key.startsWith('on')) element.addEventListener(key.slice(2), value);
    else if (key === 'class') element.className = value;
    else element.setAttribute(key, value);
  }
  for (const child of children.flat()) {
    if (child !== null && child !== undefined) element.append(child instanceof Node ? child : document.createTextNode(String(child)));
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

function fail(error) { notice.textContent = error.message; notice.hidden = false; }
function action(label, handler, cls = '') {
  return node('button', {type: 'button', class: cls, onclick: async event => {
    event.currentTarget.disabled = true;
    try { await handler(); notice.hidden = true; await render(); } catch (error) { fail(error); }
    finally { event.target.disabled = false; }
  }}, label);
}
function link(text, route, cls = '') { return node('a', {href: `#${route}`, class: cls}, text); }
function badge(text, cls = text.toLowerCase()) { return node('span', {class: `badge ${cls}`}, text); }
function heading(title, subtitle, ...controls) { return node('div', {class: 'page-heading'}, node('div', {}, node('h1', {}, title), node('p', {class: 'intro'}, subtitle)), node('div', {class: 'controls'}, controls)); }
function table(headers, rows) { return node('div', {class: 'panel table-wrap'}, node('table', {}, node('thead', {}, node('tr', {}, headers.map(text => node('th', {}, text)))), node('tbody', {}, rows.map(cells => node('tr', {}, cells.map(cell => node('td', {}, cell))))))); }
function empty(text) { return node('div', {class: 'panel empty'}, text); }
function date(value) { return value ? new Date(value).toLocaleString() : '—'; }
function cost(value) { return value == null ? 'Unavailable' : new Intl.NumberFormat('en-US', {style: 'currency', currency: 'USD', minimumFractionDigits: 2, maximumFractionDigits: 6}).format(value); }
function sumCosts(items) {
  const priced = items.filter(item => item.estimatedCostUsd != null);
  return {
    estimatedCostUsd: priced.length ? priced.reduce((total, item) => total + item.estimatedCostUsd, 0) : null,
    missingCostCount: items.reduce((total, item) => total + (item.missingCostCount ?? (item.finishedAt && item.estimatedCostUsd == null ? 1 : 0)), 0)
  };
}
function costSummary(summary) {
  return node('span', {class: 'cost', title: 'Estimated USD token cost, including previous attempts; actual billing may differ.'},
    cost(summary.estimatedCostUsd), summary.estimatedCostUsd != null && summary.missingCostCount ? ' (partial)' : null,
    summary.missingCostCount ? node('span', {class: 'subline'}, `${summary.missingCostCount} finished attempt${summary.missingCostCount === 1 ? '' : 's'} without cost data`) : null);
}
function totalCost(label, summary) { return node('p', {class: 'cost-total'}, node('span', {}, label), costSummary(summary)); }
function route() { const [path, query] = location.hash.slice(1).split('?'); return {path: path || 'runs', params: new URLSearchParams(query)}; }
function pagination(result, path, params) {
  const totalPages = Math.max(1, Math.ceil(result.total / result.pageSize));
  function pageLink(label, target) { const next = new URLSearchParams(params); next.set('page', target); return link(label, `${path}?${next}`); }
  return node('div', {class: 'pagination'}, result.page > 0 ? pageLink('Previous', result.page - 1) : null, `${result.total} items · Page ${result.page + 1} of ${totalPages}`, result.page + 1 < totalPages ? pageLink('Next', result.page + 1) : null);
}

function progress(run) {
  const terminal = run.completed + run.failed;
  return node('div', {class: 'progress'}, node('span', {class: 'count'}, `${run.completed} / ${run.total} reviewed`), node('div', {class: 'track'}, node('span', {style: `width:${run.total ? terminal / run.total * 100 : 0}%`})), node('span', {class: 'subline'}, `${run.created} created · ${run.running} running · ${run.failed} execution failures`));
}

async function home() {
  const data = await api('/overview');
  const active = data.runs.find(run => run.active);
  const result = [heading('Review runs', 'Compare what each model finds across the card catalog.', action('Create run', () => { dialog.showModal(); }, 'primary')), totalCost('Total estimated cost across runs', sumCosts(data.runs))];
  result.push(node('div', {class: 'section-heading'}, node('h2', {}, active ? `Workers are reviewing ${active.name}` : 'Workers are waiting'), active ? action('Pause new claims', () => api('/active-run', 'PUT', {runId: null})) : node('span', {class: 'muted'}, 'Create a run or make one active to start.')));
  if (!data.runs.length) result.push(empty('Create your first run to queue all implemented cards. Reprints share one review.'));
  else result.push(table(['Run', 'Model / level', 'Progress', 'Findings', 'Cards with findings', 'Estimated cost', ''], data.runs.map(run => [
    node('div', {}, link(run.name, `runs/${run.id}`, 'run-title'), node('span', {class: 'subline'}, date(run.createdAt)), run.active ? badge('Active') : null),
    node('div', {}, run.model, node('span', {class: 'subline'}, run.reasoningEffort)), progress(run), node('span', {class: 'number'}, run.findingCount), run.cardsWithFindings, costSummary(run),
    run.active ? null : action('Make active', () => api('/active-run', 'PUT', {runId: run.id}))
  ])));
  if (data.models.length) {
    result.push(node('div', {class: 'section-heading'}, node('h2', {}, 'Findings by model'), node('span', {class: 'muted'}, 'Totals across runs; equivalent bugs are counted separately.')));
    result.push(table(['Model', 'Reasoning level', 'Runs', 'Cards reviewed', 'Findings'], data.models.map(model => [model.model, model.reasoningEffort, model.runs, model.completed, node('span', {class: 'number'}, model.findingCount)])));
  }
  return result;
}

function filters(path, params, statuses = false) {
  const form = node('form', {class: 'controls', onsubmit: event => {
    event.preventDefault();
    const values = new URLSearchParams(new FormData(form));
    location.hash = `${path}?${values}`;
  }});
  form.append(node('input', {name: 'query', value: params.get('query') || '', placeholder: 'Filter cards…', 'aria-label': 'Filter cards'}));
  if (statuses) {
    const select = node('select', {name: 'status', 'aria-label': 'Task status'}, ['', 'CREATED', 'RUNNING', 'COMPLETED', 'FAILED'].map(status => node('option', {value: status}, status || 'All statuses')));
    select.value = params.get('status') || '';
    form.append(select);
  }
  form.append(node('button', {type: 'submit'}, 'Filter'));
  return form;
}

async function runPage(id, params) {
  const [run, tasks] = await Promise.all([api(`/runs/${id}`), api(`/runs/${id}/tasks?${params}`)]);
  const controls = run.active ? badge('Active') : action('Make active', () => api('/active-run', 'PUT', {runId: run.id}));
  const result = [link('All runs', 'runs', 'back-link'), heading(run.name, `${run.model} / ${run.reasoningEffort}`, controls), totalCost('Total estimated cost', run), progress(run), node('p', {class: 'muted'}, `${run.findingCount} findings in ${run.cardsWithFindings} cards. Catalog commit: ${run.catalogCommit || 'unavailable'}.`)];
  result.push(node('div', {class: 'section-heading'}, filters(`runs/${id}`, params, true), node('div', {class: 'controls'}, action('Requeue failed', () => api(`/runs/${id}/requeue`, 'POST', {status: 'FAILED'})), action('Requeue running', () => {
    if (confirm('Requeue all running tasks? Results from their current workers will be rejected.')) return api(`/runs/${id}/requeue`, 'POST', {status: 'RUNNING'});
  }))));
  result.push(tasks.items.length ? table(['Card', 'Printing', 'Review', 'Worker', 'Estimated cost', 'Publication', ''], tasks.items.map(task => [
    node('div', {}, link(task.cardName, `cards/${task.cardId}`, 'card-name'), node('span', {class: 'subline'}, task.className.split('.').pop())),
    `${task.setCode} ${task.collectorNumber}`, node('div', {class: 'badges'}, badge(task.status), task.outcome ? badge(task.outcome) : null, task.findingCount ? `${task.findingCount} findings` : null),
    node('div', {}, task.workerId || '—', node('span', {class: 'subline'}, task.startedAt ? date(task.startedAt) : '')), cost(task.estimatedCostUsd), task.publicationStatus || '—',
    action('Requeue', () => {
      if (task.status !== 'RUNNING' || confirm('Requeue this running task? Its current result will be rejected.')) return api(`/tasks/${task.id}/requeue`, 'POST');
    })
  ])) : empty('No cards match these filters.'));
  result.push(pagination(tasks, `runs/${id}`, params));
  return result;
}

async function catalogPage(params) {
  const cards = await api(`/cards?${params}`);
  return [heading('Card catalog', 'Find a card to compare its reviews across models and runs.'), node('div', {class: 'section-heading'}, filters('cards', params)), cards.items.length ? table(['Card', 'Implementation'], cards.items.map(card => [link(card.cardName, `cards/${card.id}`, 'card-name'), card.className])) : empty('No cards found. Create a run to populate the catalog, or try another search.'), pagination(cards, 'cards', params)];
}

function attemptBody(attempt) {
  return [attempt.findings.length ? node('ol', {class: 'findings'}, attempt.findings.map(description => node('li', {}, description))) : node('p', {class: 'muted'}, attempt.outcome === 'PASS' ? 'No bugs reported.' : attempt.outcome === 'ERROR' ? 'The review did not complete.' : 'Waiting for the worker to submit its result.'),
    node('p', {class: 'review-meta', title: 'Token estimate at configured API rates; actual billing may differ.'}, `Estimated token cost: ${cost(attempt.estimatedCostUsd)}`,
      attempt.inputTokens == null ? null : node('span', {class: 'subline'}, `${attempt.inputTokens.toLocaleString()} input tokens (${attempt.cachedInputTokens.toLocaleString()} cached) · ${attempt.outputTokens.toLocaleString()} output tokens`)),
    attempt.executionError ? node('p', {class: 'failure'}, attempt.executionError) : null,
    attempt.publicationError ? node('p', {class: 'failure'}, `Publication failed: ${attempt.publicationError}`) : null,
    node('div', {class: 'review-meta'}, `Worker: ${attempt.workerId}`, node('br'), `Started: ${date(attempt.startedAt)}`, node('br'), `Finished: ${date(attempt.finishedAt)}`, node('br'), `Reviewed commit: ${attempt.reviewedCommit || '—'}`, node('br'), `Publication: ${attempt.publicationStatus || 'pending'}`, attempt.publishedCommit ? node('div', {}, `Published commit: ${attempt.publishedCommit}`) : null)
  ];
}

async function cardPage(id) {
  const card = await api(`/cards/${id}`);
  return [link('Card catalog', 'cards', 'back-link'), heading(card.cardName, card.className), totalCost('Total estimated cost for this card', sumCosts(card.reviews.flatMap(review => review.attempts))), node('div', {class: 'badges'}, card.printings.map(printing => node('span', {class: 'printing'}, `${printing.setCode} ${printing.collectorNumber}`))),
    node('div', {class: 'comparison'}, card.reviews.map(review => {
      const current = review.attempts.find(attempt => attempt.current);
      const history = review.attempts.filter(attempt => !attempt.current);
      return node('article', {class: 'review'}, node('div', {class: 'review-heading'}, node('div', {}, node('h3', {}, link(review.run.name, `runs/${review.run.id}`)), node('p', {class: 'muted'}, `${review.run.model} / ${review.run.reasoningEffort}`)), badge(current?.outcome || review.status)),
        totalCost('Estimated review cost', sumCosts(review.attempts)),
        current ? attemptBody(current) : node('p', {class: 'muted'}, 'This card is waiting to be claimed.'),
        history.length ? node('details', {}, node('summary', {}, `${history.length} previous attempts`), history.map(attempt => node('div', {class: 'history-item'}, badge(attempt.outcome || 'Requeued'), attemptBody(attempt)))) : null,
        action('Requeue review', () => {
          if (review.status !== 'RUNNING' || confirm('Requeue this running review? Its current result will be rejected.')) return api(`/tasks/${review.id}/requeue`, 'POST');
        })
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
    if (version === generation) {
      content.replaceChildren(...children);
      document.getElementById('refresh-note').textContent = `Updated ${new Date().toLocaleTimeString()}`;
    }
  } catch (error) { if (version === generation) fail(error); }
}

document.getElementById('global-search').addEventListener('submit', event => { event.preventDefault(); location.hash = `cards?${new URLSearchParams(new FormData(event.target))}`; });
document.getElementById('close-dialog').addEventListener('click', () => dialog.close());
document.getElementById('run-form').addEventListener('submit', async event => {
  event.preventDefault();
  const submit = event.target.querySelector('[type=submit]');
  submit.disabled = true;
  submit.textContent = 'Creating run…';
  try { const run = await api('/runs', 'POST', Object.fromEntries(new FormData(event.target))); dialog.close(); notice.hidden = true; location.hash = `runs/${run.id}`; }
  catch (error) { dialog.close(); fail(error); }
  finally { submit.disabled = false; submit.textContent = 'Create run'; }
});
window.addEventListener('hashchange', () => { notice.hidden = true; render(); });
setInterval(() => { if (!document.hidden && !dialog.open && !['INPUT', 'SELECT', 'TEXTAREA'].includes(document.activeElement.tagName)) render(); }, 5000);
render();
