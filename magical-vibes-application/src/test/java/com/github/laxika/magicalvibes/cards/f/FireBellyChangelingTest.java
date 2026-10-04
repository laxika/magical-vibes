package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MadAuntie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireBellyChangeling.class, MadAuntie.class})
class FireBellyChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: gets +1/+0 until end of turn")
    void pumpGivesPlusOnePlusZero() {
        Permanent changeling = addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.getPowerModifier()).isEqualTo(1);
        assertThat(changeling.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can be activated twice, stacking to +2/+0")
    void pumpStacksTwice() {
        Permanent changeling = addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be activated more than twice each turn")
    void cannotActivateMoreThanTwice() {
        addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 2 times");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent changeling = addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(changeling.getPowerModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(changeling.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Unresolved activations count toward the twice-per-turn limit")
    void pendingActivationsCountTowardLimit() {
        Permanent changeling = addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(changeling.getPowerModifier()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 2 times");

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(changeling.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each copy has its own activation limit and boosts only itself")
    void copiesHaveSeparateLimits() {
        Permanent first = addCreatureReady(player1, new FireBellyChangeling());
        Permanent second = addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        assertThat(second.getPowerModifier()).isZero();

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 1, null, null);
            harness.passBothPriorities();
        }
        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The activation limit resets on the opponent's turn")
    void activationLimitResetsEachTurn() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(changeling.getPowerModifier()).isZero();
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        assertThat(changeling.getPowerModifier()).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 2 times");
    }

    @Test
    @DisplayName("Changeling receives a Goblin lord's bonus")
    void changelingReceivesGoblinBonus() {
        Permanent changeling = addCreatureReady(player1, new FireBellyChangeling());
        int power = gqs.getEffectivePower(gd, changeling);
        int toughness = gqs.getEffectiveToughness(gd, changeling);

        harness.addToBattlefield(player1, new MadAuntie());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(toughness + 1);
    }
}
