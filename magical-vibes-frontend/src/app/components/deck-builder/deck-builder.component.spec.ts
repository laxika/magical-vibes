import { Subject } from 'rxjs';
import { Router } from '@angular/router';
import { DeckBuilderComponent } from './deck-builder.component';
import { BrowseCardInfo, MessageType, WebsocketService } from '../../services/websocket.service';

describe('Commander deck builder', () => {
  function builder() {
    const sent: any[] = [];
    const messages = new Subject<any>();
    const ws = { availableDecks: [], availableSets: [], isConnected: () => true,
      getMessages: () => messages, onDisconnected: () => new Subject<void>(),
      send: (message: any) => sent.push(message) } as unknown as WebsocketService;
    const component = new DeckBuilderComponent({ navigate: () => {} } as unknown as Router, ws);
    component.ngOnInit();
    return { component, sent, messages };
  }
  const card = { setCode: 'TST', collectorNumber: '1', name: 'Commander', implemented: true,
    typeLine: 'Legendary Creature' } as BrowseCardInfo;

  it('moves one copy from the main deck into the commander slot', () => {
    const { component } = builder();
    component.changeFormat('COMMANDER');
    component.deckEntries.set([{ cardInfo: card, count: 2 }]);
    component.selectCommander(0, new Event('click'));
    expect(component.commander()).toEqual(card);
    expect(component.deckCardCount()).toBe(1);
    component.clearCommander();
    expect(component.commander()).toBeNull();
    expect(component.deckCardCount()).toBe(2);
    component.ngOnDestroy();
  });

  it('saves an invalid draft with format, commander and sideboard intact', () => {
    const { component, sent } = builder();
    component.deckName.set('Draft'); component.changeFormat('COMMANDER'); component.commander.set(card);
    component.sideboard.set([{ cardInfo: card, count: 1 }]);
    component.validation.set({ errors: ['Incorrect deck size'], legalityUpdatedAt: null });
    component.saveDeck();
    expect(sent[0]).toMatchObject({ type: MessageType.SAVE_DECK, name: 'Draft', format: 'COMMANDER',
      entries: [], commander: { setCode: 'TST', collectorNumber: '1', count: 1 },
      sideboard: [{ setCode: 'TST', collectorNumber: '1', count: 1 }] });
    component.ngOnDestroy();
  });

  it('hydrates a saved deck before allowing an update of its id', () => {
    const { component, messages, sent } = builder();
    messages.next({ type: MessageType.LOAD_DECK_RESPONSE, deck: { id: 'custom-1', name: 'Saved',
      format: 'COMMANDER', entries: [], sideboard: [], commander: { setCode: 'TST', collectorNumber: '1', count: 1 } } });
    messages.next({ type: MessageType.CARD_LIST_RESPONSE, setCode: 'UNRELATED', cards: [] });
    expect(component.editingId()).toBeUndefined();
    messages.next({ type: MessageType.CARD_LIST_RESPONSE, setCode: 'TST', cards: [card] });
    expect(component.editingId()).toBe('custom-1');
    expect(component.commander()?.name).toBe('Commander');
    component.saveDeck();
    expect(sent[sent.length - 1]).toMatchObject({ type: MessageType.SAVE_DECK, id: 'custom-1' });
    component.ngOnDestroy();
  });
});
