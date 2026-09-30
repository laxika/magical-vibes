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
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isZero();

        harness.passBothPriorities();

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
    @DisplayName("The ability cannot remove an ice counter when none remain")
    void cannotRemoveIceCounterWhenNoneRemain() {
        Permanent darkDepths = addDarkDepths();
        darkDepths.setCounterCount(CounterType.ICE, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
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

    private Permanent addDarkDepths() {
        return harness.enterBattlefieldAndReturn(player1, new DarkDepths());
    }
}
