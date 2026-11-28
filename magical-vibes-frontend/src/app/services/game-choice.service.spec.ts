import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { GameChoiceService } from './game-choice.service';
import { Card, Game, InteractionPromptNotification, MessageType, WebsocketService } from './websocket.service';

const ME = 'me';

function setup(game: Game | null = null) {
  const sent: any[] = [];
  const ws = { currentUser: { userId: ME }, send: (m: unknown) => sent.push(m) };
  TestBed.configureTestingModule({
    providers: [{ provide: WebsocketService, useValue: ws }],
  });
  const service = TestBed.inject(GameChoiceService);
  service.init(signal(game), () => [], () => [], () => 0);
  return { service, sent };
}

function prompt(p: Partial<InteractionPromptNotification>): InteractionPromptNotification {
  return { type: MessageType.INTERACTION_PROMPT, prompt: 'p', ...p } as InteractionPromptNotification;
}

function card(id: string, type = 'CREATURE'): Card {
  return { id, name: id, type } as unknown as Card;
}

describe('GameChoiceService multi-permanent pick', () => {
  it('does not answer below the minimum count', () => {
    const { service, sent } = setup();
    service.handleInteractionPrompt(prompt({
      shape: 'MULTI_PERMANENT_PICK', permanentIds: ['a', 'b', 'c'], minCount: 2, maxCount: 3,
    }));

    service.toggleMultiPermanentSelection('a');
    service.confirmMultiPermanentChoice();

    expect(sent.length).toBe(0);
    expect(service.choosingMultiplePermanents).toBe(true);
    expect(service.canConfirmMultiPermanentChoice).toBe(false);

    service.toggleMultiPermanentSelection('b');
    expect(service.canConfirmMultiPermanentChoice).toBe(true);
    service.confirmMultiPermanentChoice();

    expect(sent.length).toBe(1);
    expect(sent[0].ids).toEqual(['a', 'b']);
  });

  it('allows an empty answer when no minimum was sent', () => {
    const { service, sent } = setup();
    service.handleInteractionPrompt(prompt({
      shape: 'MULTI_PERMANENT_PICK', permanentIds: ['a'], maxCount: 1,
    }));

    service.confirmMultiPermanentChoice();

    expect(sent.length).toBe(1);
  });
});

describe('GameChoiceService graveyard index pick over a card pool', () => {
  it('shows the pool cards the server sent, not a guess from the graveyards', () => {
    const land = card('land', 'LAND');
    const bear = card('bear');
    const game = {
      playerIds: [ME, 'opp'], playerNames: ['Me', 'Opp'], graveyards: [[land], [bear]],
    } as unknown as Game;
    const { service, sent } = setup(game);
    service.handleInteractionPrompt(prompt({
      shape: 'GRAVEYARD_INDEX_PICK', cardIndices: [0, 1], cards: [land, bear], allGraveyards: true,
    }));

    const shown = service.graveyardChoiceCards;
    expect(shown.map(e => e.card.id)).toEqual(['land', 'bear']);
    expect(shown.map(e => e.owner)).toEqual(['Me', 'Opp']);

    service.chooseGraveyardCard(0);
    expect(sent[0].index).toBe(0);
  });

  it('shows a pool card that is not in a graveyard as exiled', () => {
    const bear = card('bear');
    const hidden = { id: 'hidden', name: 'Face-down card' } as unknown as Card;
    const game = {
      playerIds: [ME, 'opp'], playerNames: ['Me', 'Opp'], graveyards: [[], [bear]],
    } as unknown as Game;
    const { service } = setup(game);
    service.handleInteractionPrompt(prompt({
      shape: 'GRAVEYARD_INDEX_PICK', cardIndices: [0, 1], cards: [bear, hidden], allGraveyards: true,
    }));

    const shown = service.graveyardChoiceCards;
    expect(shown.map(e => e.card.name)).toEqual(['bear', 'Face-down card']);
    expect(shown.map(e => e.owner)).toEqual(['Opp', 'Exile']);
  });
});
