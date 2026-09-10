package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MantaRiders.class})
class MantaRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Manta Riders has no flying by default")
    void noFlyingByDefault() {
        Permanent riders = addCreatureReady(player1, new MantaRiders());

        assertThat(gqs.hasKeyword(gd, riders, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("{U}: Manta Riders gains flying until end of turn")
    void activationGrantsFlying() {
        Permanent riders = addCreatureReady(player1, new MantaRiders());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, riders, Keyword.FLYING)).isTrue();
        assertThat(riders.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Ability requires blue mana")
    void requiresBlueMana() {
        addCreatureReady(player1, new MantaRiders());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate while summoning sick because the ability does not require tapping")
    void canActivateWhileSummoningSick() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new MantaRiders());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, riders, Keyword.FLYING)).isTrue();
        assertThat(riders.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent riders = addCreatureReady(player1, new MantaRiders());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, riders, Keyword.FLYING)).isFalse();
    }
}
