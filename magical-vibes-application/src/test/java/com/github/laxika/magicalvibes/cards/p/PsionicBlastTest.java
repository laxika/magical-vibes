package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsionicBlast.class, GrizzlyBears.class})
class PsionicBlastTest extends BaseCardTest {

    @Test
    void dealsFourDamageToTargetCreatureAndTwoDamageToController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PsionicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void dealsFourDamageToTargetPlayerAndTwoDamageToController() {
        harness.setHand(player1, List.of(new PsionicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }
}
