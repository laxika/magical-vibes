package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgeOfZeal.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class})
class SurgeOfZealTest extends BaseCardTest {

    @Test
    @DisplayName("Gives haste to the target and every creature sharing a color with it")
    void grantsHasteToTargetAndColorSharingCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownMatchingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentMatchingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(ownMatchingCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opponentMatchingCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(differentColorCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A colorless target affects only itself")
    void colorlessTargetOnlyAffectsItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(otherColorlessCreature.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(coloredCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SurgeOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }
}
