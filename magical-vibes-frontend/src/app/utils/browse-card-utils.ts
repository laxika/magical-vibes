import { BrowseCardInfo, Card } from '../services/websocket.service';

/** Maps a card-browser/deck-builder card info to the Card shape the card renderer expects. */
export function browseInfoToCard(info: BrowseCardInfo): Card {
  const powerNum = info.power != null ? (parseInt(info.power, 10) || 0) : null;
  const toughnessNum = info.toughness != null ? (parseInt(info.toughness, 10) || 0) : null;

  return {
    id: null,
    name: info.name,
    type: info.type,
    additionalTypes: info.additionalTypes ?? [],
    supertypes: info.supertypes ?? [],
    subtypes: info.subtypes ?? [],
    cardText: info.cardText,
    manaCost: info.manaCost,
    power: powerNum,
    toughness: toughnessNum,
    keywords: info.keywords ?? [],
    hasTapAbility: false,
    setCode: info.setCode,
    collectorNumber: info.collectorNumber,
    color: info.color,
    needsTarget: false,
    needsSpellTarget: false,
    activatedAbilities: [],
    loyalty: info.loyalty,
    hasConvoke: false,
    colors: (info.colors && info.colors.length > 0) ? info.colors : (info.color ? [info.color] : []),
    hasPhyrexianMana: (info.manaCost ?? '').includes('/P'),
    phyrexianManaCount: ((info.manaCost ?? '').match(/\/P/g) || []).length,
    token: false,
    watermark: null,
    hasAlternateCastingCost: false,
    alternateCostLifePayment: 0,
    alternateCostSacrificeCount: 0,
    alternateCostTapCount: 0,
    alternateCostReturnCount: 0,
    alternateCostManaCost: null,
    graveyardActivatedAbilities: [],
    transformable: false,
    kickerCost: null,
    modalChoicesRequired: 0,
    modalChoicesMax: 0,
    modalOptional: false,
    modalOptions: null
  };
}
