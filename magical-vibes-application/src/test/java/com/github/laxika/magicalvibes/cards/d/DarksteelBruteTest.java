package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(brute.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, brute)).isTrue();
        assertThat(gqs.isArtifact(brute)).isTrue();
        assertThat(gqs.getEffectivePower(gd, brute)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(2);
        assertThat(brute.getTransientSubtypes()).contains(CardSubtype.BEAST);
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

        assertThat(brute.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, brute)).isFalse();
        assertThat(brute.getTransientSubtypes()).doesNotContain(CardSubtype.BEAST);
    }

    private Permanent addBruteReady() {
        Permanent permanent = new Permanent(new DarksteelBrute());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
