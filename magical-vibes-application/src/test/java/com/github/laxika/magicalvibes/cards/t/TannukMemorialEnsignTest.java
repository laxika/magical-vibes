package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EmergencyEject;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TannukMemorialEnsign.class, Forest.class, EmergencyEject.class})
class TannukMemorialEnsignTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall deals damage each time and draws on the second resolution")
    void secondLandfallResolutionDrawsCard() {
        harness.addToBattlefield(player1, new TannukMemorialEnsign());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());

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
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());

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
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Queued landfall abilities draw only as the second ability resolves")
    void queuedLandfallsCountResolutionsRatherThanTriggers() {
        harness.addToBattlefield(player1, new TannukMemorialEnsign());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn, new Forest()));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The resolution count resets each turn and landfall works on an opponent's turn")
    void resolutionCountResetsOnOpponentsTurn() {
        harness.addToBattlefield(player1, new TannukMemorialEnsign());
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        resolveLandfall(new Forest());
        resolveLandfall(new Forest());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("The second landfall still deals damage and draws after Tannuk is destroyed")
    void queuedSecondResolutionWorksAfterSourceLeaves() {
        Permanent tannuk = harness.addToBattlefieldAndReturn(player1, new TannukMemorialEnsign());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());

        resolveLandfall(new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player2, List.of(new EmergencyEject()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, tannuk.getId());

        harness.assertNotOnBattlefield(player1, "Tannuk, Memorial Ensign");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A new Tannuk starts a separate resolution count in the same turn")
    void replacementTannukStartsWithFreshCount() {
        Permanent tannuk = harness.addToBattlefieldAndReturn(player1, new TannukMemorialEnsign());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        resolveLandfall(new Forest());

        harness.setHand(player2, List.of(new EmergencyEject()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, tannuk.getId());
        harness.assertNotOnBattlefield(player1, "Tannuk, Memorial Ensign");
        harness.enterBattlefieldAndReturn(player1, new TannukMemorialEnsign());

        resolveLandfall(new Forest());
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveLandfall(new Forest());
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private void resolveLandfall(Card land) {
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        gd.playerHands.get(player1.getId()).add(land);
        harness.playLand(player1, gd.playerHands.get(player1.getId()).size() - 1);
        harness.passBothPriorities();
    }
}
