package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainfulBond.class, Forest.class, GrizzlyBears.class})
class PainfulBondTest extends BaseCardTest {

    @Test
    void drawsTwoThenMarksAllNonlandCardsInHand() {
        GrizzlyBears handCreature = new GrizzlyBears();
        Forest handLand = new Forest();
        GrizzlyBears drawnCreature = new GrizzlyBears();
        Forest drawnLand = new Forest();
        harness.setHand(player1, List.of(new PainfulBond(), handCreature, handLand));
        harness.setLibrary(player1, List.of(drawnCreature, drawnLand));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(handCreature, handLand, drawnCreature, drawnLand);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(handCreature));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(drawnCreature));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);

        harness.playLand(player1, gd.playerHands.get(player1.getId()).indexOf(handLand));
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
