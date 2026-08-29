package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SphinxOfMagosiTest extends BaseCardTest {

    @Test
    @DisplayName("Ability draws a card and puts a +1/+1 counter on Sphinx of Magosi")
    void abilityDrawsAndAddsCounter() {
        Permanent sphinx = addSphinxReady(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(sphinx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityRequiresMana() {
        Permanent sphinx = addSphinxReady(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(sphinx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addSphinxReady(Player player) {
        return addCreatureReady(player, new SphinxOfMagosi());
    }
}
