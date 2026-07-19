package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BouncePermanentOnUpkeepEffectHandlerTest {

    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private BouncePermanentOnUpkeepEffectHandler handler;

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
        handler = new BouncePermanentOnUpkeepEffectHandler(
                predicateEvaluationService, gameBroadcastService, playerInputService);
    }

    // ===== Helper methods =====

    private Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private Permanent createCreature(String name) {
        Card card = createCard(name);
        card.setType(CardType.CREATURE);
        return new Permanent(card);
    }

    private Permanent createArtifact(String name) {
        Card card = createCard(name);
        card.setType(CardType.ARTIFACT);
        return new Permanent(card);
    }

    private StackEntry entryWithSource(Card card, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName() + " trigger", effects, (UUID) null, sourcePermanentId);
    }

    private StackEntry entryWithTargetAndSource(Card card, UUID controllerId, List<CardEffect> effects,
                                                UUID targetId, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName() + " trigger", effects, targetId, sourcePermanentId);
    }

    @Test
    @DisplayName("TRIGGER_TARGET_PLAYER scope prompts the target player to choose")
    void triggerTargetPlayerScopePromptsTargetPlayer() {
        Card card = createCard("Sunken Hope");
        Permanent creature = createCreature("Grizzly Bears");
        gd.playerBattlefields.get(player2Id).add(creature);

        BouncePermanentOnUpkeepEffect effect = new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER, Set.of(),
                "Choose a creature to return to its owner's hand.");
        StackEntry entry = entryWithTargetAndSource(card, player1Id,
                List.of(effect), player2Id, UUID.randomUUID());

        when(predicateEvaluationService.matchesFilters(eq(creature), eq(Set.of()), any())).thenReturn(true);

        handler.resolve(gd, entry, effect);

        verify(playerInputService).beginPermanentChoice(eq(gd), eq(player2Id),
                eq(List.of(creature.getId())), anyString());
    }

    @Test
    @DisplayName("SOURCE_CONTROLLER scope prompts the source controller to choose")
    void sourceControllerScopePromptsController() {
        Card card = createCard("Esperzoa");
        Permanent artifact = createArtifact("Fire Diamond");
        gd.playerBattlefields.get(player1Id).add(artifact);

        BouncePermanentOnUpkeepEffect effect = new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER, Set.of(),
                "Choose an artifact to return.");
        StackEntry entry = entryWithSource(card, player1Id,
                List.of(effect), UUID.randomUUID());

        when(predicateEvaluationService.matchesFilters(eq(artifact), eq(Set.of()), any())).thenReturn(true);

        handler.resolve(gd, entry, effect);

        verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                eq(List.of(artifact.getId())), anyString());
    }

    @Test
    @DisplayName("Sets BounceCreature permanent choice context")
    void setsBounceContext() {
        Card card = createCard("Sunken Hope");
        Permanent creature = createCreature("Grizzly Bears");
        gd.playerBattlefields.get(player1Id).add(creature);

        BouncePermanentOnUpkeepEffect effect = new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER, Set.of(),
                "Choose a creature.");
        StackEntry entry = entryWithTargetAndSource(card, player1Id,
                List.of(effect), player1Id, UUID.randomUUID());

        when(predicateEvaluationService.matchesFilters(eq(creature), eq(Set.of()), any())).thenReturn(true);

        handler.resolve(gd, entry, effect);

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Logs and skips when no valid permanents exist")
    void logsWhenNoValidPermanents() {
        Card card = createCard("Esperzoa");
        // No permanents on player1's battlefield

        BouncePermanentOnUpkeepEffect effect = new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER, Set.of(),
                "Choose an artifact.");
        StackEntry entry = entryWithSource(card, player1Id,
                List.of(effect), UUID.randomUUID());

        handler.resolve(gd, entry, effect);

        verify(gameBroadcastService).logAndBroadcast(eq(gd),
                argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("controls no valid permanents")));
        verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Only offers permanents matching filters as valid choices")
    void filtersRestrictValidChoices() {
        Card card = createCard("Esperzoa");
        Permanent matchingArtifact = createArtifact("Fire Diamond");
        Permanent nonMatchingCreature = createCreature("Grizzly Bears");
        gd.playerBattlefields.get(player1Id).add(matchingArtifact);
        gd.playerBattlefields.get(player1Id).add(nonMatchingCreature);

        BouncePermanentOnUpkeepEffect effect = new BouncePermanentOnUpkeepEffect(
                BouncePermanentOnUpkeepEffect.Scope.SOURCE_CONTROLLER, Set.of(),
                "Choose an artifact.");
        StackEntry entry = entryWithSource(card, player1Id,
                List.of(effect), UUID.randomUUID());

        when(predicateEvaluationService.matchesFilters(eq(matchingArtifact), any(), any())).thenReturn(true);
        when(predicateEvaluationService.matchesFilters(eq(nonMatchingCreature), any(), any())).thenReturn(false);

        handler.resolve(gd, entry, effect);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                idsCaptor.capture(), anyString());

        assertThat(idsCaptor.getValue())
                .contains(matchingArtifact.getId())
                .doesNotContain(nonMatchingCreature.getId());
    }
}
