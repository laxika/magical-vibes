package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cinderclasm.class, GrizzlyBears.class, HillGiant.class})
class CinderclasmTest extends BaseCardTest {

    @Test
    void withoutKickerDealsOneDamageToEachCreature() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Cinderclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownBear.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingGiant.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void whenKickedDealsTwoDamageToEachCreature() {
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Cinderclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownGiant.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingGiant.getMarkedDamage()).isEqualTo(2);
    }
}
