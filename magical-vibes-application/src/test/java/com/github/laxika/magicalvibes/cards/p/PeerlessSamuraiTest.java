package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
