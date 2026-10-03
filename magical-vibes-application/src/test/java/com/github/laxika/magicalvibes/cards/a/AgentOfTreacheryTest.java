package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SowerOfTemptation;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentOfTreachery.class, Forest.class, GrizzlyBears.class, SowerOfTemptation.class, Unsummon.class})
class AgentOfTreacheryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains control of any target permanent permanently")
    void etbGainsControlOfAnyPermanentPermanently() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AgentOfTreachery()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, 0, forest.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(forest.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(forest.getId()));
    }

    @Test
    @DisplayName("Draws three cards at your end step with three permanents you do not own")
    void drawsThreeCardsWithThreePermanentsNotOwned() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(
                new SowerOfTemptation(), new SowerOfTemptation(), new AgentOfTreachery()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        castAndResolveCreature(firstCreature.getId());
        castAndResolveCreature(secondCreature.getId());
        castAndResolveCreature(land.getId());

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("Does not draw at your end step with fewer than three permanents you do not own")
    void doesNotDrawWithFewerThanThreePermanentsNotOwned() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SowerOfTemptation(), new AgentOfTreachery()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        castAndResolveCreature(creature.getId());
        castAndResolveCreature(secondCreature.getId());

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void castAndResolveCreature(UUID targetId) {
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();
    }

    @Test
    void controlPersistsAfterAgentLeavesBattlefield() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AgentOfTreachery(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        castAndResolveCreature(land.getId());

        Permanent agent = findPermanent(player1, "Agent of Treachery");
        harness.castAndResolveInstant(player1, 0, agent.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(agent);
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        stealThreePermanents(false);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void rechecksOwnershipCountWhenEndStepAbilityResolves() {
        Permanent stolenAgent = stealThreePermanents(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Unsummon()));
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(4);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, stolenAgent.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card instanceof AgentOfTreachery);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void eachAgentDrawsThreeCardsForPermanentsStolenByOtherAgents() {
        stealThreePermanents(false);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    private Permanent stealThreePermanents(boolean stealAgent) {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent third = stealAgent
                ? harness.addToBattlefieldAndReturn(player2, new AgentOfTreachery())
                : harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(
                new AgentOfTreachery(), new AgentOfTreachery(), new AgentOfTreachery()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 15);
        castAndResolveCreature(first.getId());
        castAndResolveCreature(second.getId());
        castAndResolveCreature(third.getId());
        return third;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
