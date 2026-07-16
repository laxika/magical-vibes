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
    description: 'This is the battlefield where all permanents (creatures, lands, artifacts, enchantments) live. Your permanents are on the bottom half, your opponent\'s are on top. The board always fits your screen — cards shrink as more permanents enter play.',
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
    description: 'Your creatures sit above your lands. You can see their power/toughness on the card. Creatures with summoning sickness (just entered the battlefield) appear slightly faded and can\'t attack yet. Hover any card to see a full-size preview in the side panel.',
    targetSelector: '.my-creatures-row',
    tooltipPosition: 'bottom'
  },
  {
    id: 'your-lands',
    title: 'Your Lands',
    description: 'Your lands appear here at the bottom of the battlefield. Identical basic lands are fanned into a single stack to save space. Tapped lands appear rotated sideways.',
    targetSelector: '.my-lands-row',
    tooltipPosition: 'top'
  },
  {
    id: 'phase-tracker',
    title: 'Phase Tracker',
    description: 'This rail shows which step of the turn you\'re in. The highlighted step is the current one — hover a step to see its name. You can click steps to set auto-stops — the game will pause and give you priority at those steps. Main phases always auto-stop.',
    targetSelector: '.phase-tracker',
    tooltipPosition: 'right'
  },
  {
    id: 'shortcuts',
    title: 'Keyboard Shortcuts',
    description: 'The keyboard icon at the bottom of the phase rail lists the shortcuts: Space or Enter passes priority (and confirms attackers or blockers in combat), and Esc cancels targeting, mode, or ability choices.',
    targetSelector: '.shortcuts-popup',
    tooltipPosition: 'right'
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
    description: 'The mana strip in the side panel shows your floating mana. There\'s a socket for each color plus colorless — when you have mana of a color, its socket lights up with a count. The Forest you just tapped added one green mana.',
    targetSelector: '.mana-strip',
    tooltipPosition: 'left'
  },
  {
    id: 'hand',
    title: 'Your Hand',
    description: 'These are the cards in your hand. Cards with a gold outline are castable right now. In a real game, clicking a castable card starts casting it — then you tap lands to pay its cost, and the spell fires once the mana is paid. You can cancel any time before that.',
    targetSelector: '.hand-area',
    tooltipPosition: 'top'
  },
  {
    id: 'side-panel',
    title: 'Side Panel',
    description: 'The side panel shows a full-size preview of whatever card you hover. Below it are tabs for the game log, the stack (spells waiting to resolve), and both graveyards.',
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
    description: 'The action bar contains the "Pass Priority" button. When both players pass priority in succession, the top spell on the stack resolves (or the game moves to the next step if the stack is empty). During combat this is where you confirm attackers and blockers.',
    targetSelector: '.action-bar',
    tooltipPosition: 'left'
  },
  {
    id: 'surrender',
    title: 'Surrender',
    description: 'Click your own player name badge to open the player menu, where you can surrender the game. Use this if you want to concede a match.',
    targetSelector: '.turn-info .player-badge-wrapper:last-child',
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
