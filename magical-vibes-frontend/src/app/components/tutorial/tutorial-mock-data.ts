import { Game, GameStatus, TurnStep, Card, Permanent, StackEntry } from '../../services/websocket.service';

function makeCard(overrides: Partial<Card>): Card {
  return {
    id: null,
    name: '',
    type: 'CREATURE',
    additionalTypes: [],
    supertypes: [],
    subtypes: [],
    cardText: null,
    manaCost: null,
    power: null,
    toughness: null,
    keywords: [],
    hasTapAbility: false,
    setCode: null,
    collectorNumber: null,
    color: null,
    colors: [],
    needsTarget: false,
    needsSpellTarget: false,
    activatedAbilities: [],
    loyalty: null,
    hasConvoke: false,
    hasPhyrexianMana: false,
    phyrexianManaCount: 0,
    token: false,
    watermark: null,
    hasAlternateCastingCost: false,
    alternateCostLifePayment: 0,
    alternateCostSacrificeCount: 0,
    graveyardActivatedAbilities: [],
    transformable: false,
    ...overrides
  };
}

function makePermanent(id: string, card: Card, overrides?: Partial<Permanent>): Permanent {
  return {
    id,
    card,
    tapped: false,
    attacking: false,
    blocking: false,
    blockingTargets: [],
    summoningSick: false,
    powerModifier: 0,
    toughnessModifier: 0,
    grantedKeywords: [],
    removedKeywords: [],
    effectivePower: card.power ?? 0,
    effectiveToughness: card.toughness ?? 0,
    chosenColor: null,
    chosenName: null,
    regenerationShield: 0,
    attachedTo: null,
    cantBeBlocked: false,
    animatedCreature: false,
    loyaltyCounters: 0,
    chargeCounters: 0,
    phylacteryCounters: 0,
    slimeCounters: 0,
    wishCounters: 0,
    attackTargetId: null,
    markedDamage: 0,
    transformed: false,
    ...overrides
  };
}

// --- Card definitions ---

const forestCard = makeCard({
  name: 'Forest',
  type: 'LAND',
  supertypes: ['BASIC'],
  subtypes: ['FOREST'],
  color: null,
  colors: [],
  setCode: 'ISD',
  collectorNumber: '264',
  hasTapAbility: true,
  activatedAbilities: [{
    description: '{T}: Add {G}.',
    requiresTap: true,
    needsTarget: false,
    needsSpellTarget: false,
    manaCost: null,
    loyaltyCost: null,
    minTargets: 0,
    maxTargets: 0,
    isManaAbility: true,
    variableLoyaltyCost: false
  }]
});

const plainsCard = makeCard({
  name: 'Plains',
  type: 'LAND',
  supertypes: ['BASIC'],
  subtypes: ['PLAINS'],
  color: null,
  colors: [],
  setCode: 'ISD',
  collectorNumber: '250',
  hasTapAbility: true,
  activatedAbilities: [{
    description: '{T}: Add {W}.',
    requiresTap: true,
    needsTarget: false,
    needsSpellTarget: false,
    manaCost: null,
    loyaltyCost: null,
    minTargets: 0,
    maxTargets: 0,
    isManaAbility: true,
    variableLoyaltyCost: false
  }]
});

const islandCard = makeCard({
  name: 'Island',
  type: 'LAND',
  supertypes: ['BASIC'],
  subtypes: ['ISLAND'],
  color: null,
  colors: [],
  setCode: 'ISD',
  collectorNumber: '253',
  hasTapAbility: true,
  activatedAbilities: [{
    description: '{T}: Add {U}.',
    requiresTap: true,
    needsTarget: false,
    needsSpellTarget: false,
    manaCost: null,
    loyaltyCost: null,
    minTargets: 0,
    maxTargets: 0,
    isManaAbility: true,
    variableLoyaltyCost: false
  }]
});

const swampCard = makeCard({
  name: 'Swamp',
  type: 'LAND',
  supertypes: ['BASIC'],
  subtypes: ['SWAMP'],
  color: null,
  colors: [],
  setCode: 'ISD',
  collectorNumber: '258',
  hasTapAbility: true,
  activatedAbilities: [{
    description: '{T}: Add {B}.',
    requiresTap: true,
    needsTarget: false,
    needsSpellTarget: false,
    manaCost: null,
    loyaltyCost: null,
    minTargets: 0,
    maxTargets: 0,
    isManaAbility: true,
    variableLoyaltyCost: false
  }]
});

const avacynsPilgrimCard = makeCard({
  name: "Avacyn's Pilgrim",
  type: 'CREATURE',
  subtypes: ['HUMAN', 'MONK'],
  manaCost: '{G}',
  power: 1,
  toughness: 1,
  color: 'GREEN',
  colors: ['GREEN'],
  setCode: 'ISD',
  collectorNumber: '170',
  cardText: '{T}: Add {W}.',
  hasTapAbility: true,
  activatedAbilities: [{
    description: '{T}: Add {W}.',
    requiresTap: true,
    needsTarget: false,
    needsSpellTarget: false,
    manaCost: null,
    loyaltyCost: null,
    minTargets: 0,
    maxTargets: 0,
    isManaAbility: true,
    variableLoyaltyCost: false
  }]
});

const battlegroundGeistCard = makeCard({
  name: 'Battleground Geist',
  type: 'CREATURE',
  subtypes: ['SPIRIT'],
  manaCost: '{4}{U}',
  power: 3,
  toughness: 3,
  color: 'BLUE',
  colors: ['BLUE'],
  setCode: 'ISD',
  collectorNumber: '45',
  cardText: 'Flying\nOther Spirit creatures you control get +1/+0.',
  keywords: ['FLYING']
});

// Extra card for the hand
const anotherPilgrimCard = makeCard({
  ...avacynsPilgrimCard,
  id: 'hand-pilgrim-1'
});

const elderOfLaurels = makeCard({
  name: 'Elder of Laurels',
  type: 'CREATURE',
  subtypes: ['HUMAN', 'ADVISOR'],
  manaCost: '{2}{G}',
  power: 2,
  toughness: 3,
  color: 'GREEN',
  colors: ['GREEN'],
  setCode: 'ISD',
  collectorNumber: '177',
  cardText: '{3}{G}: Target creature gets +X/+X until end of turn, where X is the number of creatures you control.'
});

const travelPreparations = makeCard({
  name: 'Travel Preparations',
  type: 'SORCERY',
  manaCost: '{1}{G}',
  color: 'GREEN',
  colors: ['GREEN'],
  setCode: 'ISD',
  collectorNumber: '206',
  cardText: 'Put a +1/+1 counter on each of up to two target creatures.\nFlashback {1}{W}'
});

// --- Permanents ---

const myBattlefield: Permanent[] = [
  makePermanent('forest-1', forestCard),
  makePermanent('forest-2', forestCard),
  makePermanent('forest-3', forestCard, { tapped: true }),
  makePermanent('plains-1', plainsCard),
  makePermanent('plains-2', plainsCard),
  makePermanent('pilgrim-1', avacynsPilgrimCard),
];

const opponentBattlefield: Permanent[] = [
  makePermanent('opp-island-1', islandCard),
  makePermanent('opp-island-2', islandCard),
  makePermanent('opp-swamp-1', swampCard),
  makePermanent('opp-geist-1', battlegroundGeistCard, { effectivePower: 3, effectiveToughness: 3 }),
];

// --- Mock Game ---

export const TUTORIAL_MOCK_GAME: Game = {
  id: 'tutorial',
  gameName: 'Tutorial',
  status: GameStatus.RUNNING,
  playerNames: ['You', 'Opponent'],
  playerIds: ['tutorial-player', 'tutorial-opponent'],
  gameLog: [
    'Game started.',
    'You play Forest.',
    'Opponent plays Island.',
    'You play Plains.',
    'Opponent plays Island.',
    "You cast Avacyn's Pilgrim.",
    'Opponent plays Swamp.',
    'You play Forest.',
    'Opponent casts Battleground Geist.',
    'You play Forest.',
  ],
  currentStep: TurnStep.PRECOMBAT_MAIN,
  activePlayerId: 'tutorial-player',
  turnNumber: 5,
  priorityPlayerId: 'tutorial-player',
  hand: [
    anotherPilgrimCard,
    elderOfLaurels,
    travelPreparations,
  ],
  opponentHand: [],
  mulliganCount: 0,
  deckSizes: [53, 53],
  handSizes: [3, 4],
  battlefields: [myBattlefield, opponentBattlefield],
  manaPool: {},
  autoStopSteps: [TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN],
  lifeTotals: [20, 20],
  poisonCounters: [0, 0],
  stack: [],
  graveyards: [[], []],
  revealedLibraryTopCards: [[], []],
  mindControlledPlayerId: null
};

/** Index of the first untapped Forest in myBattlefield (for the tap-land interactive step). */
export const TUTORIAL_TAP_FOREST_INDEX = 0;
