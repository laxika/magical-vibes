package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardsControlLossWatch;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageTriggerContextEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
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
 * Covers the merged exile-top-cards-to-source effect across all {@link LibraryScope} values: the
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
        handler = new ExileTopCardsToSourceEffectHandler(gameQueryService, gameLogService, exileService);
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
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id,
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
                    inv.getArgument(3), true);
            return null;
        }).when(exileService).exileCardFaceDown(any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONTROLLER scope exiles only the controller's library, face down")
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
        verify(exileService, times(2)).exileCardFaceDown(any(), eq(player1Id), any(), any());
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
    @DisplayName("TARGET_OPPONENT scope exiles from the bound target player, not the controller")
    void targetOpponentScopeUsesBoundTarget() {
        Card sourceCard = card("Nightveil Specter");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).addAll(List.of(card("Mine1"), card("Mine2")));
        gd.playerDecks.get(player2Id).addAll(List.of(card("Theirs1"), card("Theirs2")));

        var effect = new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_OPPONENT);
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceUp();

        handler.resolve(gd, entry(sourceCard, effect, player2Id, source.getId()), effect);

        assertThat(gd.playerDecks.get(player1Id)).hasSize(2);
        assertThat(gd.playerDecks.get(player2Id)).hasSize(1);
        verify(exileService).exileCard(any(), eq(player2Id), any(), any());
    }

    @Test
    @DisplayName("TARGET_PLAYER scope exiles from the chosen player, including the controller")
    void targetPlayerScopeUsesChosenPlayer() {
        Card sourceCard = card("Mindreaver");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).addAll(List.of(card("Mine1"), card("Mine2")));
        gd.playerDecks.get(player2Id).addAll(List.of(card("Theirs1"), card("Theirs2")));

        var effect = new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_PLAYER);
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceUp();

        handler.resolve(gd, entry(sourceCard, effect, player1Id, source.getId()), effect);

        assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
        assertThat(gd.playerDecks.get(player2Id)).hasSize(2);
        verify(exileService).exileCard(any(), eq(player1Id), any(), any());
    }

    @Test
    @DisplayName("TARGET_OPPONENT scope falls back to the sole opponent when no target is bound")
    void targetOpponentScopeFallsBackToSoleOpponent() {
        Card sourceCard = card("Grimoire Thief");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player2Id).addAll(List.of(card("A"), card("B"), card("C"), card("D")));

        var effect = new ExileTopCardsToSourceEffect(3, true, false, LibraryScope.TARGET_OPPONENT);
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceDown();

        handler.resolve(gd, entry(sourceCard, effect, null, source.getId()), effect);

        assertThat(gd.playerDecks.get(player2Id)).hasSize(1);
        verify(exileService, times(3)).exileCardFaceDown(any(), eq(player2Id), any(), any());
    }

    @Test
    @DisplayName("EACH_PLAYER scope exiles from every library, capped at each library's size")
    void eachPlayerScopeExilesFromEveryLibrary() {
        Card sourceCard = card("Knowledge Pool");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).addAll(List.of(card("A"), card("B"), card("C")));
        gd.playerDecks.get(player2Id).addAll(List.of(card("X"), card("Y")));

        var effect = new ExileTopCardsToSourceEffect(3, false, false, LibraryScope.EACH_PLAYER);
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
    @DisplayName("EACH_PLAYER scope logs nothing for a player with an empty library")
    void eachPlayerScopeSkipsEmptyLibraries() {
        Card sourceCard = card("Knowledge Pool");
        Permanent source = addPermanent(player1Id, sourceCard);
        gd.playerDecks.get(player1Id).add(card("A"));

        var effect = new ExileTopCardsToSourceEffect(3, false, false, LibraryScope.EACH_PLAYER);
        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        stubExileFaceUp();

        handler.resolve(gd, entry(sourceCard, effect, null, source.getId()), effect);

        assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(1);
        verify(gameLogService, times(1)).append(eq(gd), any(GameLogEntry.class));
    }

    @Test
    @DisplayName("Fizzles when the source permanent has left the battlefield")
    void fizzlesWhenSourceGone() {
        UUID sourceId = UUID.randomUUID();
        Card sourceCard = card("Knowledge Pool");
        gd.playerDecks.get(player1Id).add(card("A"));

        var effect = new ExileTopCardsToSourceEffect(3, false, false, LibraryScope.EACH_PLAYER);
        when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(null);

        handler.resolve(gd, entry(sourceCard, effect, null, sourceId), effect);

        assertThat(gd.exiledCards.stream().anyMatch(e -> e.sourcePermanentId() != null)).isFalse();
        assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
    }

    @Test
    @DisplayName("Uses the active Adventure face for persistent exile permissions")
    void usesAdventureFaceForPersistentPermission() {
        Card sourceCard = card("Decadent Dragon");
        Card adventureFace = card("Expensive Taste");
        adventureFace.addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, false, 0, null, false, false, false, true));
        sourceCard.setBackFaceCard(adventureFace);
        gd.playerDecks.get(player2Id).add(card("Exiled card"));

        var effect = new ExileTopCardsToSourceEffect(1, true, false,
                LibraryScope.TARGET_OPPONENT, true);
        StackEntry adventureEntry = entry(sourceCard, effect, player2Id, null);
        adventureEntry.setCastWithAdventure(true);
        stubExileFaceDown();

        handler.resolve(gd, adventureEntry, effect);

        Card exiledCard = gd.getPlayerExiledCards(player2Id).getFirst();
        assertThat(gd.exilePlayPermissions).containsEntry(exiledCard.getId(), player1Id);
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

    @Test
    @DisplayName("Only the opponent scope asks for a combat-damage trigger context")
    void combatDamageTriggerContextIsScopeDependent() {
        assertThat(new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_OPPONENT)
                .combatDamageTriggerContext())
                .isEqualTo(CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER);
        assertThat(new ExileTopCardsToSourceEffect(7).combatDamageTriggerContext()).isNull();
        assertThat(new ExileTopCardsToSourceEffect(3, false, false, LibraryScope.EACH_PLAYER)
                .combatDamageTriggerContext()).isNull();
        assertThat(new ExileTopCardsToSourceEffect(3, false, false, LibraryScope.TARGET_PLAYER)
                .combatDamageTriggerContext()).isNull();
    }
}
