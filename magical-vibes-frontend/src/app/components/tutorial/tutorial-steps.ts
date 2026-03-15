export interface TutorialStep {
  id: string;
  title: string;
  description: string;
  targetSelector: string;
  tooltipPosition: 'top' | 'bottom' | 'left' | 'right' | 'center';
  interactive?: boolean;
  interactionHint?: string;
  advanceButtonText?: string;
}

export const TUTORIAL_STEPS: TutorialStep[] = [
  {
    id: 'welcome',
    title: 'Welcome to Magical Vibes!',
    description: 'This tutorial will walk you through the game interface so you know where everything is before your first real match. No server connection needed — this is a fake battlefield for learning purposes.',
    targetSelector: '',
    tooltipPosition: 'center',
    advanceButtonText: 'Let\'s go!'
  },
  {
    id: 'battlefield',
    title: 'The Battlefield',
    description: 'This is the battlefield where all permanents (creatures, lands, artifacts, enchantments) live. Your permanents are on the bottom half, your opponent\'s are on top.',
    targetSelector: '.battlefield-area',
    tooltipPosition: 'bottom'
  },
  {
    id: 'opponent-side',
    title: 'Opponent\'s Side',
    description: 'Your opponent\'s permanents are displayed here. Their lands are at the very top, and creatures sit below them — mirroring your layout.',
    targetSelector: '.opponent-battlefield',
    tooltipPosition: 'bottom'
  },
  {
    id: 'your-creatures',
    title: 'Your Creatures',
    description: 'Your creatures sit above your lands. You can see their power/toughness on the card. Creatures with summoning sickness (just entered the battlefield) appear slightly faded and can\'t attack yet.',
    targetSelector: '.my-creatures-row',
    tooltipPosition: 'bottom'
  },
  {
    id: 'your-lands',
    title: 'Your Lands',
    description: 'Your lands appear here at the bottom of the battlefield. Identical basic lands are stacked together to save space. Tapped lands appear rotated sideways.',
    targetSelector: '.my-lands-row',
    tooltipPosition: 'top'
  },
  {
    id: 'phase-tracker',
    title: 'Phase Tracker',
    description: 'The phase tracker shows which step of the turn you\'re in. The highlighted step is the current one. You can click steps to set auto-stops — the game will pause and give you priority at those steps. Main phases always auto-stop.',
    targetSelector: '.phase-tracker',
    tooltipPosition: 'bottom'
  },
  {
    id: 'tap-land',
    title: 'Tap a Land',
    description: 'Tap one of your untapped Forests to add green mana to your mana pool. In a real game, you\'d tap lands to pay for spells.',
    targetSelector: '.tutorial-interactive-target',
    tooltipPosition: 'right',
    interactive: true,
    interactionHint: 'Click an untapped Forest to tap it for mana.'
  },
  {
    id: 'mana-pool',
    title: 'Mana Pool',
    description: 'Your mana pool appears here when you have floating mana. The colored circles show how much mana of each color you have available to spend on spells and abilities.',
    targetSelector: '.mana-pool-bar',
    tooltipPosition: 'bottom'
  },
  {
    id: 'hand',
    title: 'Your Hand',
    description: 'These are the cards in your hand. Cards with a gold outline are playable right now — you have enough mana and it\'s the right phase. In a real game, clicking a playable card will cast it.',
    targetSelector: '.hand-area',
    tooltipPosition: 'top'
  },
  {
    id: 'side-panel',
    title: 'Side Panel',
    description: 'The side panel shows detailed card info when you hover over any card. It also has tabs for the stack (spells waiting to resolve), graveyard, and game log.',
    targetSelector: 'app-side-panel',
    tooltipPosition: 'left'
  },
  {
    id: 'player-info',
    title: 'Player Info',
    description: 'The player info section shows each player\'s name, life total, hand size, and deck size. Badges indicate who is the active player and who holds priority.',
    targetSelector: '.turn-info',
    tooltipPosition: 'left'
  },
  {
    id: 'action-bar',
    title: 'Pass Priority',
    description: 'The action bar contains the "Pass Priority" button. When both players pass priority in succession, the top spell on the stack resolves (or the game moves to the next step if the stack is empty).',
    targetSelector: '.action-bar',
    tooltipPosition: 'left'
  },
  {
    id: 'surrender',
    title: 'Surrender',
    description: 'Click your own player name badge to open the player menu, where you can surrender the game. Use this if you want to concede a match.',
    targetSelector: '.turn-info .player-badge-wrapper:first-child',
    tooltipPosition: 'left'
  },
  {
    id: 'done',
    title: 'Ready to Play!',
    description: 'You now know the basics of the game interface. Create a game from the lobby to start playing for real. Good luck!',
    targetSelector: '',
    tooltipPosition: 'center',
    advanceButtonText: 'Back to Lobby'
  }
];
