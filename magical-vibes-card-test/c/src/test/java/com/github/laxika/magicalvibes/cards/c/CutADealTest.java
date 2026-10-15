package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlhammarretsArchive;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.s.StinkweedImp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CutADeal.class, Forest.class, NarsetParterOfVeils.class,
        AlhammarretsArchive.class, StinkweedImp.class})
class CutADealTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent draws one, then the controller draws for each opponent who drew")
    void eachOpponentDrawsThenControllerDraws() {
        harness.setHand(player1, List.of(new CutADeal()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Cut a Deal");
    }

    @Test
    void preventedOpponentDrawDoesNotGiveControllerACard() {
        harness.setHand(player1, List.of(new CutADeal()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new CutADeal()));
        harness.setLibrary(player2, List.of(new CutADeal(), new CutADeal()));
        harness.enterBattlefieldAndReturn(player1, new NarsetParterOfVeils());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Cut a Deal");
    }

    @Test
    void opponentDrawingTwoStillCountsAsOnlyOneOpponent() {
        harness.setHand(player1, List.of(new CutADeal()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new CutADeal(), new CutADeal()));
        harness.setLibrary(player2, List.of(new CutADeal(), new CutADeal()));
        harness.addToBattlefield(player2, new AlhammarretsArchive());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Cut a Deal");
    }

    @Test
    void dredgingOpponentDoesNotGiveControllerACard() {
        StinkweedImp imp = new StinkweedImp();
        harness.setHand(player1, List.of(new CutADeal()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new CutADeal()));
        harness.setLibrary(player2, List.of(new CutADeal(), new CutADeal(), new CutADeal(),
                new CutADeal(), new CutADeal()));
        harness.setGraveyard(player2, List.of(imp));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(imp);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        harness.assertInGraveyard(player1, "Cut a Deal");
    }

    @Test
    void decliningDredgeResumesOpponentDrawBeforeControllerDraw() {
        StinkweedImp imp = new StinkweedImp();
        CutADeal opponentTop = new CutADeal();
        CutADeal controllerTop = new CutADeal();
        harness.setHand(player1, List.of(new CutADeal()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(controllerTop));
        harness.setLibrary(player2, List.of(opponentTop, new CutADeal(), new CutADeal(),
                new CutADeal(), new CutADeal()));
        harness.setGraveyard(player2, List.of(imp));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleGraveyardCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerTop);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(imp);
        harness.assertInGraveyard(player1, "Cut a Deal");
    }
}
