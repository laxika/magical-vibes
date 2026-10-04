package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Breathstealer.class)
class BreathstealerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/-1")
    void activatingAbilityBoosts() {
        Permanent breathstealer = addCreatureReady(player1, new Breathstealer());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, breathstealer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, breathstealer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations lower toughness to 0, putting it into the graveyard")
    void repeatedActivationsCanKillIt() {
        Permanent breathstealer = addCreatureReady(player1, new Breathstealer());
        harness.addMana(player1, ManaColor.BLACK, 2);

        // 2/2 -> +1/-1 -> 3/1, then +1/-1 again -> 4/0 -> dies to state-based action.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(breathstealer);
        harness.assertInGraveyard(player1, "Breathstealer");
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent breathstealer = addCreatureReady(player1, new Breathstealer());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, breathstealer)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, breathstealer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, breathstealer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability works while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent breathstealer = addCreatureReady(player1, new Breathstealer());
        breathstealer.setSummoningSick(true);
        breathstealer.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, breathstealer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, breathstealer)).isEqualTo(1);
        assertThat(breathstealer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost applies only when the activated ability resolves")
    void boostWaitsForResolution() {
        Permanent breathstealer = addCreatureReady(player1, new Breathstealer());
        Permanent other = addCreatureReady(player1, new Breathstealer());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, breathstealer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, breathstealer)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, breathstealer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, breathstealer)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability requires black mana")
    void cannotActivateWithoutBlackMana() {
        Permanent breathstealer = addCreatureReady(player1, new Breathstealer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, breathstealer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, breathstealer)).isEqualTo(2);
    }
}
