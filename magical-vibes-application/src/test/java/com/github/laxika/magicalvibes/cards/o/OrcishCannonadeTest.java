package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishCannonade.class, GrizzlyBears.class, HillGiant.class})
class OrcishCannonadeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a creature, deals 3 damage to its controller, and draws a card")
    void damagesCreatureDamagesControllerAndDrawsCard() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new OrcishCannonade()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to a player, deals 3 damage to the caster, and draws a card")
    void damagesPlayerDamagesCasterAndDrawsCard() {
        harness.setHand(player1, List.of(new OrcishCannonade()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
