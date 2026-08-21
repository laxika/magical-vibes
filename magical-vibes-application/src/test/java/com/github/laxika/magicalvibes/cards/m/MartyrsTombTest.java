package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartyrsTomb.class, GrizzlyBears.class, Shock.class})
class MartyrsTombTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 2 life and prevents the next 1 damage to a target creature")
    void paysLifeAndPreventsNextDamageToCreature() {
        harness.addToBattlefield(player1, new MartyrsTomb());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(bears.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new MartyrsTomb());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
