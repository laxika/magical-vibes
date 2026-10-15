package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed(VulshokFactory.class)
class VulshokFactoryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds red mana and a charge counter")
    void tappingAddsRedManaAndChargeCounter() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new VulshokFactory());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(factory.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(factory.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice creates a hasty X/X artifact Golem using the factory's charge counters")
    void sacrificeCreatesHastyGolemUsingChargeCounters() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new VulshokFactory());
        factory.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vulshok Factory");
        harness.assertInGraveyard(player1, "Vulshok Factory");

        Permanent golem = findPermanent(player1, "Golem");
        assertThat(golem.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardTypes(gd, golem)).contains(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.hasEffectiveSubtype(gd, golem, CardSubtype.GOLEM)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The sacrifice ability is sorcery speed")
    void sacrificeAbilityIsSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new VulshokFactory());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
