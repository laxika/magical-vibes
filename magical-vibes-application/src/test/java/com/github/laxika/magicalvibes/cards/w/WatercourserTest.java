package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Watercourser.class})
class WatercourserTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/-1")
    void activatingAbilityBoostsPowerAndLowersToughness() {
        Permanent courser = addCreatureReady(player1, new Watercourser());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(courser.getPowerModifier()).isEqualTo(1);
        assertThat(courser.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Activating three times kills it via state-based actions")
    void activatingThreeTimesKillsIt() {
        addCreatureReady(player1, new Watercourser());
        harness.addMana(player1, ManaColor.BLUE, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Watercourser");
        harness.assertInGraveyard(player1, "Watercourser");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent courser = addCreatureReady(player1, new Watercourser());
        harness.addMana(player1, ManaColor.BLUE, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(courser.getPowerModifier()).isEqualTo(2);
        assertThat(courser.getToughnessModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(courser.getPowerModifier()).isEqualTo(0);
        assertThat(courser.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent courser = harness.addToBattlefieldAndReturn(player1, new Watercourser());
        courser.setSummoningSick(true);
        courser.setTapped(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(courser.getPowerModifier()).isZero();
        assertThat(courser.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(courser.getPowerModifier()).isEqualTo(1);
        assertThat(courser.getToughnessModifier()).isEqualTo(-1);
        assertThat(courser.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pending activations do not boost another Watercourser after the source dies")
    void pendingActivationsDoNotAffectAnotherCourser() {
        addCreatureReady(player1, new Watercourser());
        Permanent other = addCreatureReady(player1, new Watercourser());
        harness.addMana(player1, ManaColor.BLUE, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        harness.assertInGraveyard(player1, "Watercourser");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
