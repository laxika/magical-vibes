package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CutthroatNegotiator.class, Forest.class, GrizzlyBears.class})
class CutthroatNegotiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates one tapped Treasure per revealed nonland and draws the revealed cards")
    void parleyCreatesTreasuresForNonlandsAndDraws() {
        Card player1Top = new GrizzlyBears();
        Card player2Top = new GrizzlyBears();
        Card player1Next = new Forest();
        Card player2Next = new Forest();
        harness.setLibrary(player1, List.of(player1Top, player1Next));
        harness.setLibrary(player2, List.of(player2Top, player2Next));
        addReadyNegotiator();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(2);
        assertThat(treasures).allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Top);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Next);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Next);
    }

    @Test
    @DisplayName("Revealed lands do not create Treasures")
    void parleyDoesNotRewardLands() {
        Card player1Top = new Forest();
        Card player2Top = new Forest();
        harness.setLibrary(player1, List.of(player1Top));
        harness.setLibrary(player2, List.of(player2Top));
        addReadyNegotiator();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Top);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Top);
    }

    @Test
    @DisplayName("A mixed reveal rewards only the attacking Negotiator's controller")
    void mixedRevealRewardsOpponentController() {
        Card player1Top = new GrizzlyBears();
        Card player2Top = new Forest();
        harness.setLibrary(player1, List.of(player1Top));
        harness.setLibrary(player2, List.of(player2Top));
        addCreatureReady(player2, new CutthroatNegotiator());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Top);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Top);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger a nonattacking Negotiator")
    void anotherCreatureAttackingDoesNotTriggerParley() {
        Card player1Top = new GrizzlyBears();
        Card player2Top = new Forest();
        harness.setLibrary(player1, List.of(player1Top));
        harness.setLibrary(player2, List.of(player2Top));
        addReadyNegotiator();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Top);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(player1Top);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(player2Top);
    }

    private Permanent addReadyNegotiator() {
        return addCreatureReady(player1, new CutthroatNegotiator());
    }
}
