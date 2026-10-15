package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.o.ObservantAlseid;
import com.github.laxika.magicalvibes.cards.r.RayOfDissolution;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LagonnaBandElder.class, ObservantAlseid.class, RayOfDissolution.class})
class LagonnaBandElderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger gains 3 life when its controller controls an enchantment")
    void gainsLifeWithControlledEnchantment() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new ObservantAlseid());
        castLagonnaBandElder();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("ETB trigger does not gain life without a controlled enchantment")
    void doesNotGainLifeWithoutControlledEnchantment() {
        harness.setLife(player1, 10);
        castLagonnaBandElder();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("An opponent's enchantment does not satisfy the condition")
    void opponentEnchantmentDoesNotSatisfyCondition() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player2, new ObservantAlseid());
        castLagonnaBandElder();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("No ability triggers if the enchantment arrives only after Elder enters")
    void enchantmentArrivingAfterEntryDoesNotCreateTrigger() {
        harness.setLife(player1, 10);
        castLagonnaBandElder();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.enterBattlefieldAndReturn(player1, new ObservantAlseid());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("An enchantment arriving while Elder is a spell satisfies the entry condition")
    void enchantmentArrivingBeforeEntrySatisfiesCondition() {
        harness.setLife(player1, 10);
        castLagonnaBandElder();
        harness.enterBattlefieldAndReturn(player1, new ObservantAlseid());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Removing the last enchantment in response prevents the life gain")
    void conditionIsCheckedAgainOnResolution() {
        harness.setLife(player1, 10);
        var enchantment = harness.addToBattlefieldAndReturn(player1, new ObservantAlseid());
        castLagonnaBandElder();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, enchantment.getId());
        harness.assertInGraveyard(player1, "Observant Alseid");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("A remaining enchantment satisfies the condition after another is destroyed")
    void remainingEnchantmentStillAllowsExactlyThreeLife() {
        harness.setLife(player1, 10);
        var enchantment = harness.addToBattlefieldAndReturn(player1, new ObservantAlseid());
        harness.addToBattlefield(player1, new ObservantAlseid());
        castLagonnaBandElder();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, enchantment.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    private void castLagonnaBandElder() {
        harness.setHand(player1, List.of(new LagonnaBandElder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
