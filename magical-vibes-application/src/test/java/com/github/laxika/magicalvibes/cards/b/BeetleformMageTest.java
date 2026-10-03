package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BeetleformMage.class})
class BeetleformMageTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants +2/+2 and flying until end of turn")
    void abilityBoostsAndGrantsFlying() {
        Permanent mage = addCreatureReady(player1, new BeetleformMage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated only once each turn")
    void abilityOncePerTurn() {
        addCreatureReady(player1, new BeetleformMage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void effectsWearOff() {
        Permanent mage = addCreatureReady(player1, new BeetleformMage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.FLYING)).isFalse();
    }

    @Test
    void cannotActivateAgainWhileFirstActivationIsOnStack() {
        addCreatureReady(player1, new BeetleformMage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
    }

    @Test
    void tappedSummoningSickMageCanActivate() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new BeetleformMage());
        mage.setSummoningSick(true);
        mage.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.FLYING)).isTrue();
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    void eachMageHasItsOwnActivationLimit() {
        Permanent first = addCreatureReady(player1, new BeetleformMage());
        Permanent second = addCreatureReady(player1, new BeetleformMage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        for (Permanent mage : new Permanent[]{first, second}) {
            assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, mage, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    void canActivateAgainDuringOpponentsTurn() {
        Permanent mage = addCreatureReady(player1, new BeetleformMage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.FLYING)).isFalse();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.FLYING)).isTrue();
    }
}
