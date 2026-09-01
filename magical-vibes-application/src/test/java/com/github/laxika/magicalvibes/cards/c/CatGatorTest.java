package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CatGator.class, Forest.class, GrizzlyBears.class, Swamp.class})
class CatGatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to the number of Swamps you control")
    void etbDealsDamageForControlledSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAt(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB counts only Swamps controlled by Cat-Gator's controller")
    void etbIgnoresOpponentsSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAt(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can target a player")
    void etbCanTargetPlayer() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new CatGator()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new CatGator()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
