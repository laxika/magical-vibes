package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvishHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and makes the target creature skip its next untap step")
    void tapsItselfAndSkipsTargetUntap() {
        Permanent hunter = addHunter();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        payActivationCost();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(hunter.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addHunter();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        payActivationCost();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHunter() {
        return addCreatureReady(player1, new ElvishHunter());
    }

    private void payActivationCost() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
