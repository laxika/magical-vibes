package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InflameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature dealt damage this turn")
    void damagesOnlyCreaturesDealtDamageThisTurn() {
        Permanent damagedCreature = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        Permanent undamagedCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new Shock(), new Inflame()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, damagedCreature.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(damagedCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(undamagedCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage creatures that have not been dealt damage this turn")
    void doesNothingWithoutPreviouslyDamagedCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Inflame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
    }
}
