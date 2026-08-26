package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparkRupture.class, SarkhanTheMasterless.class, Forest.class})
class SparkRuptureTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card")
    void entersAndDrawsACard() {
        harness.setHand(player1, List.of(new SparkRupture()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Turns planeswalkers with loyalty into creatures with loyalty-based power and toughness")
    void turnsPlaneswalkersIntoLoyaltyBasedCreatures() {
        harness.addToBattlefield(player1, new SparkRupture());
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        sarkhan.setSummoningSick(false);

        assertThat(gqs.isCreature(gd, sarkhan)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(5);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Power and toughness track loyalty counters and the effect stops at zero")
    void tracksLoyaltyCounters() {
        harness.addToBattlefield(player1, new SparkRupture());
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 3);

        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(3);

        sarkhan.setCounterCount(CounterType.LOYALTY, 6);
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(6);

        sarkhan.setCounterCount(CounterType.LOYALTY, 0);
        assertThat(gqs.isCreature(gd, sarkhan)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isTrue();
    }
}
