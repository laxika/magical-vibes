function compactFace(card) {
  const fields = {
    name: card.faceName || card.name,
    mana_cost: card.manaCost,
    type_line: card.type,
    oracle_text: card.text?.replace(/^\[([+\u2212-]?[0-9X]+)\]:/gm, '$1:'),
    power: card.power,
    toughness: card.toughness,
    loyalty: card.loyalty,
    defense: card.defense,
    colors: card.colors && [...card.colors].sort(),
  };
  return Object.fromEntries(Object.entries(fields).filter(([, value]) =>
    value !== undefined && value !== null && value !== ''));
}

/** Converts English MTGJSON face entries into compact printing records. */
export function compactMtgjsonCards(entries, setCode) {
  const printings = new Map();
  for (const card of entries) {
    if (card.language && card.language !== 'English') continue;
    if (!card.number || !card.name) throw new Error('MTGJSON card is missing its number or name');
    const number = String(card.number);
    if (!printings.has(number)) printings.set(number, new Map());
    const sides = printings.get(number);
    if (!sides.has(card.side || 'a')) sides.set(card.side || 'a', card);
  }

  return [...printings].map(([number, sides]) => {
    const ordered = [...sides].sort(([left], [right]) => left.localeCompare(right))
      .map(([, card]) => card);
    const front = ordered[0];
    const card = {
      ...compactFace(front),
      set: setCode,
      collector_number: number,
      layout: front.layout,
      color_identity: [...(front.colorIdentity || [])].sort(),
      keywords: [...new Set(ordered.flatMap((face) => face.keywords || []))],
    };
    if (ordered.length > 1) {
      card.name = front.name;
      card.card_faces = ordered.map(compactFace);
      delete card.oracle_text;
      if (['split', 'aftermath'].includes(front.layout)) {
        card.layout = 'split';
        card.mana_cost = ordered.map((face) => face.manaCost || '').join('');
        card.type_line = ordered.map((face) => face.type).join(' // ');
        card.colors = [...new Set(ordered.flatMap((face) => face.colors || []))].sort();
      } else if (['transform', 'modal_dfc', 'double_faced_token', 'reversible_card'].includes(front.layout)) {
        for (const field of ['mana_cost', 'power', 'toughness', 'loyalty', 'defense', 'colors']) {
          delete card[field];
        }
        card.type_line = ordered.map((face) => face.type).join(' // ');
      }
    }
    return card;
  });
}

/**
 * Collects the rulings of every English printing, keyed by collector number. MTGJSON repeats a
 * card's rulings on each face entry, so they are merged without duplicates. Its rulings are the
 * official Gatherer rulings, hence the `wotc` source.
 */
export function mtgjsonRulings(entries) {
  const byNumber = new Map();
  for (const card of entries) {
    if (card.language && card.language !== 'English') continue;
    if (!card.number) continue;
    const number = String(card.number);
    if (!byNumber.has(number)) byNumber.set(number, new Map());
    const rulings = byNumber.get(number);
    for (const ruling of card.rulings || []) {
      const key = `${ruling.date}\u0000${ruling.text}`;
      if (!rulings.has(key)) rulings.set(key, { date: ruling.date, source: 'wotc', text: ruling.text });
    }
  }
  return new Map([...byNumber].map(([number, rulings]) => [number, [...rulings.values()]]));
}
