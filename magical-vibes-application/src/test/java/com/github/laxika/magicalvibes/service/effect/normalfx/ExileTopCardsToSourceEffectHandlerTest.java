package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardsControlLossWatch;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the merged exile-top-cards-to-source effect across its player subjects: the
 * controller's own library (Colfenor's Plans / Duplicity / Search the City), a chosen player's
 * (Mindreaver), a single opponent's (Grimoire Thief / Nightveil Specter), and every player's
 * (Knowledge Pool).
 */
@ExtendWith(MockitoExtension.class)
class ExileTopCardsToSourceEffectHandlerTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameLogService gameLogService;
    @Mock private ExileService exileService;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ExileTopCardsToSourceEffectHandler handler;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        handler = new ExileTopCardsToSourceEffectHandler(gameQueryService, gameLogService, exileService,
                new com.github.laxika.magicalvibes.service.effect.SubjectResolutionService(gameQueryService,
                        org.mockito.Mockito.mock(com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService.class),
                        org.mockito.Mockito.mock(com.github.laxika.magicalvibes.service.target.TargetLegalityService.class)));
    }

    private Card card(String name) {
        Card c = new Card();
        c.setName(name);
        c.setType(CardType.ARTIFACT);
        return c;
    }

    private Permanent addPermanent(UUID playerId, Card c) {
        Permanent perm = new Permanent(c);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    private StackEntry entry(Card sourceCard, ExileTopCardsToSourceEffect effect,
                             UUID targetId, UUID sourcePermanentId) {
        return TestStackDoor.triggered(sourceCard, player1Id,
                sourceCard.getName(), List.of(effect), targetId, sourcePermanentId);
    }

    /** Makes the mocked exileService actually record cards so assertions on GameData work. */
    private void stubExileFaceUp() {
        doAnswer(inv -> {
            ((GameData) inv.getArgument(0)).addToExile(inv.getArgument(1), inv.getArgument(2),
                    inv.getArgument(3));
            return null;
        }).when(exileService).exileCard(any(), any(), any(), any());
    }

    private void stubExileFaceDown() {
        doAnswer(inv -> {
            ((GameData) inv.getArgument(0)).addToExile(inv.getArgument(1), inv.getArgument(2),
                    inv.getArgument(3), true, inv.getArgument(4));
            return null;
        }).when(exileService).exileCardFaceDown(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("you() exiles only the controller's library, face down")
    void controllerScopeExilesOwnLibraryFaceDown() {
        Card sourceCard = card("Colfenor's Plans");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).addAll(List.of(card("A"), card("B"), card("C")));
        gd.playerDecks.get(player2Id).addAll(List.of(card("X"), card("Y")));

        var effect = new ExileTopCardsToSourceEffect(2);
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceDown();

        handler.resolve(gd, entry(sourceCard, effect, null, source.getId()), effect);

        assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
        assertThat(gd.playerDecks.get(player2Id)).hasSize(2);
        verify(exileService, times(2)).exileCardFaceDown(any(), eq(player1Id), any(), any(), eq(player1Id));
    }

    @Test
    @DisplayName("toGraveyardOnControlLoss registers the source for the control-loss watch")
    void registersControlLossWatch() {
        Card sourceCard = card("Duplicity");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).add(card("A"));

        var effect = new ExileTopCardsToSourceEffect(1, true, true);
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceDown();

        handler.resolve(gd, entry(sourceCard, effect, null, source.getId()), effect);

        assertThat(gd.exiledCardsToGraveyardOnControlLossWatch)
                .containsEntry(source.getId(), new ExiledCardsControlLossWatch(player1Id, sourceCard));
    }

    @Test
    @DisplayName("Control-loss watch is left alone when the flag is off")
    void noControlLossWatchWhenFlagOff() {
        Card sourceCard = card("Search the City");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).add(card("A"));

        var effect = new ExileTopCardsToSourceEffect(1, false);
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceUp();

        handler.resolve(gd, entry(sourceCard, effect, null, source.getId()), effect);

        assertThat(gd.exiledCardsToGraveyardOnControlLossWatch).isEmpty();
        verify(exileService, times(1)).exileCard(any(), eq(player1Id), any(), any());
    }

    @Test
    @DisplayName("triggeringPlayer() exiles from the player the trigger recorded, not the controller")
    void triggeringPlayerExilesFromRecordedPlayer() {
        Card sourceCard = card("Nightveil Specter");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).addAll(List.of(card("Mine1"), card("Mine2")));
        gd.playerDecks.get(player2Id).addAll(List.of(card("Theirs1"), card("Theirs2")));

        var effect = new ExileTopCardsToSourceEffect(1, false, false, Subjects.triggeringPlayer());
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceUp();

        handler.resolve(gd, entry(sourceCard, effect, player2Id, source.getId()), effect);

        assertThat(gd.playerDecks.get(player1Id)).hasSize(2);
        assertThat(gd.playerDecks.get(player2Id)).hasSize(1);
        verify(exileService).exileCard(any(), eq(player2Id), any(), any());
    }

    @Test
    @DisplayName("targetPlayer() exiles from the chosen player, including the controller")
    void targetPlayerExilesFromChosenPlayer() {
        Card sourceCard = card("Mindreaver");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).addAll(List.of(card("Mine1"), card("Mine2")));
        gd.playerDecks.get(player2Id).addAll(List.of(card("Theirs1"), card("Theirs2")));

        var effect = new ExileTopCardsToSourceEffect(1, false, false, Subjects.targetPlayer());
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceUp();

        handler.resolve(gd, entry(sourceCard, effect, player1Id, source.getId()), effect);

        assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
        assertThat(gd.playerDecks.get(player2Id)).hasSize(2);
        verify(exileService).exileCard(any(), eq(player1Id), any(), any());
    }

    @Test
    @DisplayName("eachOpponent() exiles from every opponent and not from the controller")
    void eachOpponentExilesFromOpponents() {
        Card sourceCard = card("Grimoire Thief");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).addAll(List.of(card("Mine1"), card("Mine2")));
        gd.playerDecks.get(player2Id).addAll(List.of(card("A"), card("B"), card("C"), card("D")));

        var effect = new ExileTopCardsToSourceEffect(3, true, false, Subjects.eachOpponent());
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceDown();

        handler.resolve(gd, entry(sourceCard, effect, null, source.getId()), effect);

        assertThat(gd.playerDecks.get(player1Id)).hasSize(2);
        assertThat(gd.playerDecks.get(player2Id)).hasSize(1);
        verify(exileService, times(3)).exileCardFaceDown(any(), eq(player2Id), any(), any(), eq(player1Id));
    }

    @Test
    @DisplayName("eachPlayer() exiles from every library, capped at each library's size")
    void eachPlayerScopeExilesFromEveryLibrary() {
        Card sourceCard = card("Knowledge Pool");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).addAll(List.of(card("A"), card("B"), card("C")));
        gd.playerDecks.get(player2Id).addAll(List.of(card("X"), card("Y")));

        var effect = new ExileTopCardsToSourceEffect(3, false, false, Subjects.eachPlayer());
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceUp();

        handler.resolve(gd, entry(sourceCard, effect, null, source.getId()), effect);

        // Player1 exiles 3, player2 only has 2.
        assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        assertThat(gd.playerDecks.get(player2Id)).isEmpty();
        verify(gameLogService, times(2)).append(eq(gd), any(GameLogEntry.class));
    }

    @Test
    @DisplayName("eachPlayer() logs nothing for a player with an empty library")
    void eachPlayerScopeSkipsEmptyLibraries() {
        Card sourceCard = card("Knowledge Pool");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).add(card("A"));

        var effect = new ExileTopCardsToSourceEffect(3, false, false, Subjects.eachPlayer());
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceUp();

        handler.resolve(gd, entry(sourceCard, effect, null, source.getId()), effect);

        assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(1);
        verify(gameLogService, times(1)).append(eq(gd), any(GameLogEntry.class));
    }

    @Test
    @DisplayName("Still exiles cards linked to the source after it leaves the battlefield")
    void resolvesWhenSourceGone() {
        UUID sourceId = UUID.randomUUID();
        Card sourceCard = card("Knowledge Pool");
        gd.playerDecks.get(player1Id).add(card("A"));

        var effect = new ExileTopCardsToSourceEffect(3, false, false, Subjects.eachPlayer());
        when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(null);
        stubExileFaceUp();

        handler.resolve(gd, entry(sourceCard, effect, null, sourceId), effect);

        assertThat(gd.getCardsExiledByPermanent(sourceId)).hasSize(1);
        assertThat(gd.playerDecks.get(player1Id)).isEmpty();
    }

    @Test
    @DisplayName("Uses the active Adventure face for persistent exile permissions")
    void usesAdventureFaceForPersistentPermission() {
        Card sourceCard = card("Decadent Dragon");
        Card adventureFace = card("Expensive Taste");
        adventureFace.addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, false, 0, null, false, false, false, true));
        sourceCard.setBackFaceCard(adventureFace);
        gd.playerDecks.get(player2Id).add(card("Exiled card"));

        var effect = new ExileTopCardsToSourceEffect(1, true, false,
                Subjects.targetPlayer());
        StackEntry adventureEntry = entry(sourceCard, effect, player2Id, null);
        adventureEntry.setCastWithAdventure(true);
        stubExileFaceDown();

        handler.resolve(gd, adventureEntry, effect);

        Card exiledCard = gd.getPlayerExiledCards(player2Id).getFirst();
        assertThat(gd.exilePlayPermissionHolders()).containsEntry(exiledCard.getId(), player1Id);
        assertThat(gd.playerDecks.get(player2Id)).isEmpty();
    }

    @Test
    @DisplayName("Falls back to finding the source permanent by card id when the id is stale")
    void findsSourceByCardIdFallback() {
        Card sourceCard = card("Colfenor's Plans");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).add(card("A"));

        var effect = new ExileTopCardsToSourceEffect(1);
        stubExileFaceDown();

        // No source permanent id on the entry at all — the handler must find it on the battlefield.
        handler.resolve(gd, entry(sourceCard, effect, null, null), effect);

        assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(1);
    }
}
