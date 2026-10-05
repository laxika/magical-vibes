package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainfulBond.class, Forest.class, GrizzlyBears.class, HillGiant.class})
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

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(handCreature, handLand, drawnCreature, drawnLand);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(handCreature));
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(drawnCreature));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);

        harness.playLand(player1, gd.playerHands.get(player1.getId()).indexOf(handLand));
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void doesNotMarkFourManaCardsAlreadyInHandOrDrawn() {
        HillGiant handCreature = new HillGiant();
        HillGiant drawnCreature = new HillGiant();
        harness.setHand(player1, List.of(new PainfulBond(), handCreature));
        harness.setLibrary(player1, List.of(drawnCreature, new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        for (HillGiant creature : List.of(handCreature, drawnCreature)) {
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(creature));
            resolveAllTriggers();

            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }
    }

    @Test
    void repeatedResolutionsGrantSeparateLifeLossAbilities() {
        GrizzlyBears creature = new GrizzlyBears();
        PainfulBond secondBond = new PainfulBond();
        harness.setHand(player1, List.of(new PainfulBond(), secondBond, creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, gd.playerHands.get(player1.getId()).indexOf(secondBond));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(creature));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void doesNotMarkOpponentsHand() {
        harness.setHand(player1, List.of(new PainfulBond()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
