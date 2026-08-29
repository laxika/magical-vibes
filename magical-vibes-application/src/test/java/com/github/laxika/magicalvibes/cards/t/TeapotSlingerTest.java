package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeapotSlinger.class, GrizzlyBears.class})
class TeapotSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger before four total mana is spent on spells")
    void doesNotTriggerBelowExpendThreshold() {
        harness.addToBattlefield(player1, new TeapotSlinger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Deals 2 damage to each opponent when its controller expends four")
    void dealsDamageWhenControllerExpendsFour() {
        harness.addToBattlefield(player1, new TeapotSlinger());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
