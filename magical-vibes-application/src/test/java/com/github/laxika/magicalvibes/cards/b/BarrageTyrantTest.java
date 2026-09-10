package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrageTyrant.class, BronzeSable.class, GrizzlyBears.class})
class BarrageTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another colorless creature and deals damage equal to its power")
    void sacrificesAnotherColorlessCreatureAndDealsPowerDamage() {
        addCreatureReady(player1, new BarrageTyrant());
        Permanent sable = addCreatureReady(player1, new BronzeSable());
        sable.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Bronze Sable");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals the damage to a target creature")
    void dealsDamageToTargetCreature() {
        addCreatureReady(player1, new BarrageTyrant());
        addCreatureReady(player1, new BronzeSable());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice the source or a colored creature")
    void requiresAnotherColorlessCreature() {
        addCreatureReady(player1, new BarrageTyrant());
        addCreatureReady(player1, new GrizzlyBears());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
