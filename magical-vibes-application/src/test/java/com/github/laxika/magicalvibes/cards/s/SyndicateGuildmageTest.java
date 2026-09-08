package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyndicateGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("White ability taps a creature with power 4 or greater")
    void tapsHighPowerCreature() {
        addReadyGuildmage();
        Permanent target = addCreatureReady(player2, new AirElemental());
        addMana(ManaColor.WHITE, 1);
        addMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White ability cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        addReadyGuildmage();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addMana(ManaColor.WHITE, 1);
        addMana(ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Black ability deals 2 damage to an opponent")
    void damagesOpponent() {
        addReadyGuildmage();
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Black ability deals 2 damage to an opposing planeswalker")
    void damagesPlaneswalker() {
        addReadyGuildmage();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Black ability cannot target its controller or a creature")
    void requiresOpponentOrPlaneswalker() {
        addReadyGuildmage();
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyGuildmage() {
        addCreatureReady(player1, new SyndicateGuildmage());
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }
}
