package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeerlessSamurai.class, ElvishWarrior.class, GrizzlyBears.class, MindStone.class})
class PeerlessSamuraiTest extends BaseCardTest {

    @Test
    void SamuraiAttackingAloneReducesNextSpellCost() {
        addCreatureReady(player1, new PeerlessSamurai());
        harness.setHand(player1, List.of(new MindStone()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
    }

    @Test
    void WarriorAttackingAloneReducesNextSpellCost() {
        addCreatureReady(player1, new PeerlessSamurai());
        addCreatureReady(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new MindStone()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
    }

    @Test
    void NonSamuraiOrWarriorAttackingAloneDoesNotReduceNextSpellCost() {
        addCreatureReady(player1, new PeerlessSamurai());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindStone()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void MatchingCreatureAttackingWithAnotherCreatureDoesNotReduceNextSpellCost() {
        addCreatureReady(player1, new PeerlessSamurai());
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindStone()));

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(PeerlessSamurai.class)
    void MultipleSamuraiReduceTheSameNextSpell() {
        addCreatureReady(player1, new PeerlessSamurai());
        addCreatureReady(player1, new PeerlessSamurai());
        harness.setHand(player1, List.of(new PeerlessSamurai()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @CardUsed(PeerlessSamurai.class)
    void ReductionAppliesOnlyToTheNextSpell() {
        addCreatureReady(player1, new PeerlessSamurai());
        harness.setHand(player1, List.of(new PeerlessSamurai(), new PeerlessSamurai()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(PeerlessSamurai.class)
    void ReductionDoesNotPayColoredMana() {
        addCreatureReady(player1, new PeerlessSamurai());
        harness.setHand(player1, List.of(new PeerlessSamurai()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({PeerlessSamurai.class, ElvishWarrior.class})
    void SpellWithNoGenericCostStillConsumesReduction() {
        addCreatureReady(player1, new PeerlessSamurai());
        harness.setHand(player1, List.of(new ElvishWarrior(), new PeerlessSamurai()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(PeerlessSamurai.class)
    void UnusedReductionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new PeerlessSamurai());
        harness.setHand(player1, List.of(new PeerlessSamurai()));
        harness.setLibrary(player1, List.of(new PeerlessSamurai(), new PeerlessSamurai()));
        harness.setLibrary(player2, List.of(new PeerlessSamurai(), new PeerlessSamurai()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(PeerlessSamurai.class)
    void MenaceRejectsASingleBlocker() {
        addCreatureReady(player1, new PeerlessSamurai()).setAttacking(true);
        addCreatureReady(player2, new PeerlessSamurai());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("two or more creatures");
    }

    @Test
    @CardUsed(PeerlessSamurai.class)
    void MenaceAllowsTwoBlockers() {
        addCreatureReady(player1, new PeerlessSamurai()).setAttacking(true);
        addCreatureReady(player2, new PeerlessSamurai());
        addCreatureReady(player2, new PeerlessSamurai());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }
}
