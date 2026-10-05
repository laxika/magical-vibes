package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Kranioceros.class})
class KraniocerosTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+3 to Kranioceros")
    void resolvingAbilityBoostsToughness() {
        addCreatureReady(player1, new Kranioceros());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent kranioceros = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(kranioceros.getEffectivePower()).isEqualTo(5);
        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(5);
        assertThat(kranioceros.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated multiple times, boosts stack")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new Kranioceros());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent kranioceros = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new Kranioceros());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent kranioceros = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(kranioceros.getToughnessModifier()).isEqualTo(0);
        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new Kranioceros());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped, summoning-sick Kranioceros can activate using white and red mana")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent kranioceros = harness.addToBattlefieldAndReturn(player1, new Kranioceros());
        kranioceros.setSummoningSick(true);
        kranioceros.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(5);
        assertThat(kranioceros.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activation requires white mana even when two other mana are available")
    void cannotActivateWithoutWhiteMana() {
        Permanent kranioceros = addCreatureReady(player1, new Kranioceros());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability boosts only its source among multiple Kranioceros")
    void boostsOnlyItsSource() {
        Permanent other = addCreatureReady(player1, new Kranioceros());
        Permanent source = addCreatureReady(player1, new Kranioceros());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectiveToughness()).isEqualTo(5);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
    }
}
