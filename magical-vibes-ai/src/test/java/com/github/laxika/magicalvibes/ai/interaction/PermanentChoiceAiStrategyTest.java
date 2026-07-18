package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermanentChoiceAiStrategyTest {

    private final PermanentChoiceAiStrategy strategy = new PermanentChoiceAiStrategy();

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private AiGameActions gameActions;
    @Mock
    private Connection selfConnection;

    private GameData gameData;
    private UUID aiPlayerId;
    private UUID opponentId;

    @BeforeEach
    void setUp() {
        aiPlayerId = UUID.randomUUID();
        opponentId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", aiPlayerId, "AI");
        gameData.orderedPlayerIds.add(aiPlayerId);
        gameData.orderedPlayerIds.add(opponentId);
        gameData.playerBattlefields.put(aiPlayerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    @DisplayName("handledType is PermanentChoice")
    void handledType() {
        assertThat(strategy.handledType()).isEqualTo(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Registered in AiInteractionStrategies")
    void registeredInStrategies() {
        var interaction = permanentChoice(aiPlayerId, List.of(UUID.randomUUID()), null);
        assertThat(AiInteractionStrategies.forInteraction(interaction))
                .isInstanceOf(PermanentChoiceAiStrategy.class);
    }

    @Test
    @DisplayName("Wrong deciding player: does not answer")
    void ignoresWrongPlayer() throws Exception {
        strategy.answer(permanentChoice(opponentId, List.of(UUID.randomUUID()), null), context());

        verify(gameActions, never()).answerInteraction(eq(selfConnection),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Empty valid ids: does not answer")
    void ignoresEmptyValidIds() throws Exception {
        strategy.answer(permanentChoice(aiPlayerId, List.of(), null), context());

        verify(gameActions, never()).answerInteraction(eq(selfConnection),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Prefers opponent's strongest creature by effective power")
    void prefersOpponentStrongestCreature() throws Exception {
        Permanent weak = creature("Weak", "{1}", 1);
        Permanent strong = creature("Strong", "{3}", 5);
        gameData.playerBattlefields.get(opponentId).add(weak);
        gameData.playerBattlefields.get(opponentId).add(strong);

        when(gameQueryService.isCreature(gameData, weak)).thenReturn(true);
        when(gameQueryService.isCreature(gameData, strong)).thenReturn(true);
        when(gameQueryService.getEffectivePower(gameData, weak)).thenReturn(1);
        when(gameQueryService.getEffectivePower(gameData, strong)).thenReturn(5);

        strategy.answer(permanentChoice(aiPlayerId, List.of(weak.getId(), strong.getId()), null), context());

        assertChosen(strong.getId());
    }

    @Test
    @DisplayName("Falls back to opponent's highest mana-value permanent when no creatures")
    void prefersOpponentHighestManaValueNonCreature() throws Exception {
        Permanent cheap = artifact("Cheap", "{1}");
        Permanent expensive = artifact("Expensive", "{4}");
        gameData.playerBattlefields.get(opponentId).add(cheap);
        gameData.playerBattlefields.get(opponentId).add(expensive);

        when(gameQueryService.isCreature(gameData, cheap)).thenReturn(false);
        when(gameQueryService.isCreature(gameData, expensive)).thenReturn(false);

        strategy.answer(permanentChoice(aiPlayerId, List.of(cheap.getId(), expensive.getId()), null), context());

        assertChosen(expensive.getId());
    }

    @Test
    @DisplayName("Falls back to own cheapest permanent when no opponent targets")
    void prefersOwnCheapestPermanent() throws Exception {
        Permanent cheap = creature("Elves", "{G}", 1);
        Permanent expensive = creature("Angel", "{3}{W}{W}", 4);
        gameData.playerBattlefields.get(aiPlayerId).add(cheap);
        gameData.playerBattlefields.get(aiPlayerId).add(expensive);

        strategy.answer(permanentChoice(aiPlayerId, List.of(cheap.getId(), expensive.getId()), null), context());

        assertChosen(cheap.getId());
    }

    @Test
    @DisplayName("Falls back to first valid id when no matching battlefield permanent")
    void fallsBackToFirstValidId() throws Exception {
        UUID orphanId = UUID.randomUUID();

        strategy.answer(permanentChoice(aiPlayerId, List.of(orphanId), null), context());

        assertChosen(orphanId);
    }

    @Nested
    @DisplayName("Activated ability sacrifice costs")
    class SacrificeOutletPreservation {

        @Test
        @DisplayName("Preserves sac outlet and picks cheapest other fodder")
        void preservesOutletPicksCheapestFodder() throws Exception {
            Permanent seer = creature("Viscera Seer", "{B}", 1);
            Permanent ornithopter = creature("Ornithopter", "{0}", 0);
            Permanent bears = creature("Grizzly Bears", "{1}{G}", 2);
            gameData.playerBattlefields.get(aiPlayerId).addAll(List.of(seer, ornithopter, bears));

            var context = sacrificeCostChoice(seer.getId(), new SacrificeCreatureCost());
            strategy.answer(permanentChoice(aiPlayerId,
                    List.of(seer.getId(), ornithopter.getId(), bears.getId()), context), context());

            assertChosen(ornithopter.getId());
        }

        @Test
        @DisplayName("Preserves outlet for artifact sacrifice costs")
        void preservesOutletForArtifactSacrifice() throws Exception {
            Permanent outlet = artifact("Outlet", "{2}");
            Permanent cheapArtifact = artifact("Sol Ring", "{1}");
            Permanent expensiveArtifact = artifact("Expensive", "{4}");
            gameData.playerBattlefields.get(aiPlayerId).addAll(List.of(outlet, cheapArtifact, expensiveArtifact));

            var context = sacrificeCostChoice(outlet.getId(), new SacrificeArtifactCost());
            strategy.answer(permanentChoice(aiPlayerId,
                    List.of(outlet.getId(), cheapArtifact.getId(), expensiveArtifact.getId()), context),
                    context());

            assertChosen(cheapArtifact.getId());
        }

        @Test
        @DisplayName("Sacs the outlet itself when it is the only legal choice")
        void sacsOutletWhenAlone() throws Exception {
            Permanent seer = creature("Viscera Seer", "{B}", 1);
            gameData.playerBattlefields.get(aiPlayerId).add(seer);

            var context = sacrificeCostChoice(seer.getId(), new SacrificeCreatureCost());
            strategy.answer(permanentChoice(aiPlayerId, List.of(seer.getId()), context), context());

            assertChosen(seer.getId());
        }

        @Test
        @DisplayName("Does not use outlet-preservation for pay-life ability costs")
        void ignoresPayLifeAbilityCosts() throws Exception {
            Permanent source = creature("Source", "{1}", 1);
            Permanent other = creature("Other", "{0}", 0);
            Permanent oppThreat = creature("Threat", "{2}", 4);
            gameData.playerBattlefields.get(aiPlayerId).addAll(List.of(source, other));
            gameData.playerBattlefields.get(opponentId).add(oppThreat);

            when(gameQueryService.isCreature(gameData, oppThreat)).thenReturn(true);

            var payLife = sacrificeCostChoice(source.getId(), new PayLifeCost(1));
            strategy.answer(permanentChoice(aiPlayerId,
                    List.of(source.getId(), other.getId(), oppThreat.getId()), payLife), context());
            assertChosen(oppThreat.getId());
        }

        @Test
        @DisplayName("Does not use outlet-preservation for tap-creature ability costs")
        void ignoresTapCreatureAbilityCosts() throws Exception {
            Permanent source = creature("Source", "{1}", 1);
            Permanent other = creature("Other", "{0}", 0);
            Permanent oppThreat = creature("Threat", "{2}", 4);
            gameData.playerBattlefields.get(aiPlayerId).addAll(List.of(source, other));
            gameData.playerBattlefields.get(opponentId).add(oppThreat);

            when(gameQueryService.isCreature(gameData, oppThreat)).thenReturn(true);

            var tapCost = sacrificeCostChoice(source.getId(),
                    new TapCreatureCost(new PermanentIsCreaturePredicate()));
            strategy.answer(permanentChoice(aiPlayerId,
                    List.of(source.getId(), other.getId(), oppThreat.getId()), tapCost), context());
            assertChosen(oppThreat.getId());
        }

        @Test
        @DisplayName("Does not use outlet-preservation for non-ability permanent choices")
        void ignoresNonAbilityContexts() throws Exception {
            Permanent ownCheap = creature("Own", "{0}", 0);
            Permanent oppThreat = creature("Threat", "{2}", 3);
            gameData.playerBattlefields.get(aiPlayerId).add(ownCheap);
            gameData.playerBattlefields.get(opponentId).add(oppThreat);

            when(gameQueryService.isCreature(gameData, oppThreat)).thenReturn(true);

            strategy.answer(permanentChoice(aiPlayerId,
                    List.of(ownCheap.getId(), oppThreat.getId()),
                    new PermanentChoiceContext.LegendRule("Duplicate")), context());

            assertChosen(oppThreat.getId());
        }
    }

    private void assertChosen(UUID expectedId) throws Exception {
        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(eq(selfConnection), captor.capture());
        assertThat(captor.getValue()).isInstanceOf(InteractionAnswer.PermanentChosen.class);
        assertThat(((InteractionAnswer.PermanentChosen) captor.getValue()).permanentId())
                .isEqualTo(expectedId);
    }

    private AiInteractionContext context() {
        return new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions, selfConnection);
    }

    private static PendingInteraction.PermanentChoice permanentChoice(
            UUID playerId, List<UUID> validPermanentIds, PermanentChoiceContext context) {
        return new PendingInteraction.PermanentChoice(
                playerId, validPermanentIds, List.of(), context, "Choose a permanent.");
    }

    private static PermanentChoiceContext.ActivatedAbilityCostChoice sacrificeCostChoice(
            UUID sourceId, com.github.laxika.magicalvibes.model.effect.CardEffect costEffect) {
        return new PermanentChoiceContext.ActivatedAbilityCostChoice(
                UUID.randomUUID(), sourceId, 0, null, null, null, costEffect, 1);
    }

    private static Permanent creature(String name, String manaCost, int power) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setPower(power);
        card.setToughness(Math.max(1, power));
        return new Permanent(card);
    }

    private static Permanent artifact(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost(manaCost);
        return new Permanent(card);
    }
}
