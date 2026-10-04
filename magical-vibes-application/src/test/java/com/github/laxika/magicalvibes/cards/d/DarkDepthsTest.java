package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DarkDepths.class)
class DarkDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with ten ice counters")
    void entersWithTenIceCounters() {
        harness.setHand(player1, List.of(new DarkDepths()));

        harness.playLand(player1, 0);

        Permanent darkDepths = findPermanent(player1, "Dark Depths");
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isEqualTo(10);
    }

    @Test
    @DisplayName("Removing the last ice counter sacrifices Dark Depths and creates Marit Lage")
    void removesLastIceCounterAndCreatesMaritLage() {
        Permanent darkDepths = addDarkDepths();
        darkDepths.setCounterCount(CounterType.ICE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isZero();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Dark Depths");
        Permanent maritLage = findPermanent(player1, "Marit Lage");
        assertThat(maritLage.getEffectivePower()).isEqualTo(20);
        assertThat(maritLage.getEffectiveToughness()).isEqualTo(20);
        assertThat(maritLage.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(maritLage.getCard().getSubtypes()).containsExactly(CardSubtype.AVATAR);
        assertThat(maritLage.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(maritLage.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(maritLage.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The ability still costs three mana when no ice counters remain")
    void canActivateWithNoIceCounters() {
        Permanent darkDepths = addDarkDepths();
        darkDepths.setCounterCount(CounterType.ICE, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveAllTriggers();
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isZero();
    }

    @Test
    @DisplayName("The ability cannot be activated without paying three mana")
    void cannotActivateWithoutMana() {
        Permanent darkDepths = addDarkDepths();
        darkDepths.setCounterCount(CounterType.ICE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Dark Depths can remove counters repeatedly without creating a token early")
    void canRemoveCountersRepeatedlyWhileTapped() {
        Permanent darkDepths = addDarkDepths();
        darkDepths.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isEqualTo(10);
        resolveAllTriggers();

        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isEqualTo(8);
        assertThat(darkDepths.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Dark Depths");
        harness.assertNotOnBattlefield(player1, "Marit Lage");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Adding ice counters after the state trigger fires does not prevent sacrifice")
    void addedCountersDoNotPreventTriggeredSacrifice() {
        Permanent darkDepths = addDarkDepths();
        darkDepths.setCounterCount(CounterType.ICE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isZero();
        assertThat(gd.stack).hasSize(1);

        darkDepths.setCounterCount(CounterType.ICE, 2);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Dark Depths");
        harness.assertInGraveyard(player1, "Dark Depths");
        assertThat(findPermanents(player1, "Marit Lage")).hasSize(1);
    }
    @Test
    @DisplayName("No Marit Lage is created if Dark Depths leaves before the sacrifice trigger resolves")
    void noTokenWhenSourceLeavesBeforeTriggerResolves() {
        Permanent darkDepths = addDarkDepths();
        darkDepths.setCounterCount(CounterType.ICE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, darkDepths));
        resolveAllTriggers();

        harness.assertInHand(player1, "Dark Depths");
        harness.assertNotOnBattlefield(player1, "Dark Depths");
        harness.assertNotOnBattlefield(player1, "Marit Lage");
    }
    private Permanent addDarkDepths() {
        return harness.enterBattlefieldAndReturn(player1, new DarkDepths());
    }
}
