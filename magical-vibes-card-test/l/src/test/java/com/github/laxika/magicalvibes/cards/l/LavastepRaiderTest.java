package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LavastepRaider.class)
class LavastepRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{R} gives Lavastep Raider +2/+0 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent raider = addReadyRaider(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isEqualTo(2);
        assertThat(raider.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Lavastep Raider's temporary boost wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent raider = addReadyRaider(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isZero();
        assertThat(raider.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Repeated activations stack and only boost the activating Raider")
    void repeatedActivationsOnlyBoostTheirSource() {
        Permanent raider = addReadyRaider(player1);
        Permanent otherRaider = addCreatureReady(player1, new LavastepRaider());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(raider.getPowerModifier()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isEqualTo(4);
        assertThat(raider.getToughnessModifier()).isZero();
        assertThat(otherRaider.getPowerModifier()).isZero();
        assertThat(otherRaider.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Raider can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent raider = addReadyRaider(player1);
        raider.tap();
        raider.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isEqualTo(2);
        assertThat(raider.getToughnessModifier()).isZero();
        assertThat(raider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Three colorless mana cannot pay the ability's red mana requirement")
    void cannotActivateWithoutRedMana() {
        Permanent raider = addReadyRaider(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(raider.getPowerModifier()).isZero();
        assertThat(raider.getToughnessModifier()).isZero();
    }

    private Permanent addReadyRaider(Player player) {
        Permanent permanent = addCreatureReady(player, new LavastepRaider());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
