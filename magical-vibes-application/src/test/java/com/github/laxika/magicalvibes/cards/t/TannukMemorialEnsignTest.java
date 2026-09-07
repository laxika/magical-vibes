package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TannukMemorialEnsign.class, Forest.class, GrizzlyBears.class})
class TannukMemorialEnsignTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall deals damage each time and draws on the second resolution")
    void secondLandfallResolutionDrawsCard() {
        harness.addToBattlefield(player1, new TannukMemorialEnsign());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        gd.playerHands.get(player1.getId()).clear();

        resolveLandfall(new Forest());

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveLandfall(new Forest());

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The draw happens only on the exact second landfall resolution")
    void laterLandfallResolutionsDoNotDrawAgain() {
        harness.addToBattlefield(player1, new TannukMemorialEnsign());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        gd.playerHands.get(player1.getId()).clear();

        resolveLandfall(new Forest());
        resolveLandfall(new Forest());
        int handSizeAfterSecondResolution = gd.playerHands.get(player1.getId()).size();
        resolveLandfall(new Forest());

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterSecondResolution);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Tannuk")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new TannukMemorialEnsign());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.playerHands.get(player1.getId()).clear();
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void resolveLandfall(Card land) {
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        gd.playerHands.get(player1.getId()).add(land);
        harness.playLand(player1, gd.playerHands.get(player1.getId()).size() - 1);
        harness.passBothPriorities();
    }
}
