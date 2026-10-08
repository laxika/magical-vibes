package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.ScytheLeopard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValakutInvoker.class, ScytheLeopard.class})
class ValakutInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player for eight mana")
    void dealsThreeDamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals 3 damage to target creature")
    void dealsThreeDamageToCreature() {
        addReadyInvoker(player1);
        harness.addToBattlefield(player2, new ScytheLeopard());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        UUID targetId = harness.getPermanentId(player2, "Scythe Leopard");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Scythe Leopard");
        harness.assertInGraveyard(player2, "Scythe Leopard");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ValakutInvoker());
        Permanent invoker = findPermanent(player1, "Valakut Invoker");
        invoker.setSummoningSick(true);
        invoker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(invoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate twice in one turn without tapping")
    void activatesTwiceWithoutTapping() {
        harness.setLife(player2, 20);
        Permanent invoker = addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(invoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate with only seven mana")
    void requiresEightMana() {
        harness.setLife(player2, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself and dies from its own damage")
    void canTargetItself() {
        Permanent invoker = addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, invoker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Valakut Invoker");
        harness.assertInGraveyard(player1, "Valakut Invoker");
    }

    private Permanent addReadyInvoker(Player player) {
        return addCreatureReady(player, new ValakutInvoker());
    }
}
