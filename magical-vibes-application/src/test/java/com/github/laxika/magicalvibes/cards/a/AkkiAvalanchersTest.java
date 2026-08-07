package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AkkiAvalanchersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land gives +2/+0")
    void sacrificeLandBoosts() {
        Permanent akki = harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(akki.getEffectivePower()).isEqualTo(3);
        assertThat(akki.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent akki = harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(akki.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can only be activated once each turn")
    void onlyOncePerTurn() {
        harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated without a land to sacrifice")
    void requiresLand() {
        harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
