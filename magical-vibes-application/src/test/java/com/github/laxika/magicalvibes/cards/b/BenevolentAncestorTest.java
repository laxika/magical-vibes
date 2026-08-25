package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BenevolentAncestor.class, GrizzlyBears.class, Shock.class})
class BenevolentAncestorTest extends BaseCardTest {

    private void addAncestorReady() {
        Permanent ancestor = harness.addToBattlefieldAndReturn(player1, new BenevolentAncestor());
        ancestor.setSummoningSick(false);
    }

    @Test
    @DisplayName("Prevents the next 1 damage dealt to a target player")
    void preventsDamageToPlayer() {
        addAncestorReady();
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents the next 1 damage dealt to a target creature")
    void preventsDamageToCreature() {
        addAncestorReady();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player2, "Grizzly Bears")).isSameAs(bears);
    }
}
