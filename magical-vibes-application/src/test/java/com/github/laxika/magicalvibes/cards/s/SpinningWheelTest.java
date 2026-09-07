package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinningWheel.class, GrizzlyBears.class})
class SpinningWheelTest extends BaseCardTest {

    @Test
    void tapAbilityAddsChosenColor() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());

        harness.activateAbility(player1, 0, null, null);

        assertThat(wheel.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void fiveManaTapAbilityTapsTargetCreature() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(wheel.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapAbilityCannotTargetNonCreature() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, wheel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
