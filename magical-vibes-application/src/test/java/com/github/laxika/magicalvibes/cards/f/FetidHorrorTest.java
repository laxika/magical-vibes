package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed(FetidHorror.class)
class FetidHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Fetid Horror")
    void resolvingAbilityBoosts() {
        addCreatureReady(player1, new FetidHorror());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent horror = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(horror.getEffectivePower()).isEqualTo(2);
        assertThat(horror.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly if mana allows")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new FetidHorror());
        harness.addMana(player1, ManaColor.BLACK, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        Permanent horror = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(horror.getEffectivePower()).isEqualTo(4);
        assertThat(horror.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent horror = addCreatureReady(player1, new FetidHorror());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(horror.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(horror.getPowerModifier()).isEqualTo(0);
        assertThat(horror.getEffectivePower()).isEqualTo(1);
        assertThat(horror.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without black mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new FetidHorror());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated while Fetid Horror has summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent horror = addCreatureReady(player1, new FetidHorror());
        horror.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(horror.getEffectivePower()).isEqualTo(2);
        assertThat(horror.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability boosts only the Fetid Horror it was activated from")
    void onlySourceIsBoosted() {
        Permanent firstHorror = addCreatureReady(player1, new FetidHorror());
        Permanent secondHorror = addCreatureReady(player1, new FetidHorror());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(firstHorror.getEffectivePower()).isEqualTo(1);
        assertThat(firstHorror.getEffectiveToughness()).isEqualTo(2);
        assertThat(secondHorror.getEffectivePower()).isEqualTo(2);
        assertThat(secondHorror.getEffectiveToughness()).isEqualTo(3);
    }
}
