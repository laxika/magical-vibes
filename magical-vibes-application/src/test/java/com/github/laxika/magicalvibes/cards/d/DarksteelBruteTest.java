package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DarksteelBrute.class)
class DarksteelBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability makes it a 2/2 Beast artifact creature")
    void animatesIntoBeast() {
        Permanent brute = addBruteReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(brute), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, brute)).isTrue();
        assertThat(gqs.isArtifact(brute)).isTrue();
        assertThat(gqs.getEffectivePower(gd, brute)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, brute, CardSubtype.BEAST)).isTrue();
        assertThat(brute.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the ability consumes three generic mana")
    void manaIsConsumed() {
        Permanent brute = addBruteReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(brute), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        Permanent brute = addBruteReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(brute), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, brute)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, brute)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, brute, CardSubtype.BEAST)).isFalse();
    }

    private Permanent addBruteReady() {
        return addCreatureReady(player1, new DarksteelBrute());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
