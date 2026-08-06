package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormFrontTest extends BaseCardTest {

    @Test
    @DisplayName("Ability taps target creature with flying")
    void tapsFlyingCreature() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer = addCreatureReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a creature with flying its controller owns")
    void tapsOwnFlyer() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability does not tap the source, so it can be activated repeatedly")
    void canActivateRepeatedly() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer1 = addCreatureReady(player2, new AirElemental());
        Permanent flyer2 = addCreatureReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, flyer1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, flyer2.getId());
        harness.passBothPriorities();

        assertThat(flyer1.isTapped()).isTrue();
        assertThat(flyer2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new StormFront());
        Permanent flyer = addCreatureReady(player2, new AirElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
