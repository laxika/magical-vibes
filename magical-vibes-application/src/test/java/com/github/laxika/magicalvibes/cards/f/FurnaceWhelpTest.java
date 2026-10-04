package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FurnaceWhelp.class})
class FurnaceWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: gets +1/+0 until end of turn")
    void firebreathingBoostsPowerUntilEndOfTurn() {
        Permanent whelp = addCreatureReady(player1, new FurnaceWhelp());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);
    }

    @Test
    @DisplayName("Firebreathing activations stack during the turn")
    void firebreathingActivationsStack() {
        Permanent whelp = addCreatureReady(player1, new FurnaceWhelp());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);
    }

    @Test
    @DisplayName("Firebreathing can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent whelp = harness.addToBattlefieldAndReturn(player1, new FurnaceWhelp());
        whelp.setSummoningSick(true);
        whelp.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);
        assertThat(whelp.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the red activation cost")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent whelp = addCreatureReady(player1, new FurnaceWhelp());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(2);
    }

    @Test
    @DisplayName("Firebreathing uses the stack and boosts only its source")
    void boostsOnlyItsSourceOnResolution() {
        Permanent otherWhelp = addCreatureReady(player1, new FurnaceWhelp());
        Permanent sourceWhelp = addCreatureReady(player1, new FurnaceWhelp());
        Permanent opposingWhelp = addCreatureReady(player2, new FurnaceWhelp());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, sourceWhelp)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sourceWhelp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sourceWhelp)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherWhelp)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingWhelp)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
