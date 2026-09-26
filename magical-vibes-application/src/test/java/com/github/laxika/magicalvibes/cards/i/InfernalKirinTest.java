package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.d.DescendantOfKiyomaro;
import com.github.laxika.magicalvibes.cards.g.GhostLitRedeemer;
import com.github.laxika.magicalvibes.cards.g.GhostLitStalker;
import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalKirin.class, SpiritualVisit.class, GhostLitRedeemer.class,
        GhostLitStalker.class, ArabaMothrider.class, DescendantOfKiyomaro.class, HandOfHonor.class})
class InfernalKirinTest extends BaseCardTest {

    @Test
    @DisplayName("An Arcane spell makes the target player discard all cards with its mana value")
    void arcaneSpellDiscardsMatchingManaValueCards() {
        addInfernalKirin();
        harness.setHand(player2, List.of(
                new GhostLitStalker(), new GhostLitStalker(), new ArabaMothrider(),
                new DescendantOfKiyomaro()));

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Araba Mothrider", "Descendant of Kiyomaro");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ghost-Lit Stalker", "Ghost-Lit Stalker");
    }

    @Test
    @DisplayName("A Spirit spell makes the target player discard cards with mana value one")
    void spiritSpellDiscardsManaValueOneCards() {
        addInfernalKirin();
        harness.setHand(player2, List.of(new GhostLitStalker(), new HandOfHonor()));

        harness.castFromHand(player1, new GhostLitRedeemer(), "{W}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hand of Honor");
        harness.assertInGraveyard(player2, "Ghost-Lit Stalker");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        addInfernalKirin();
        harness.setHand(player2, List.of(new GhostLitStalker()));

        harness.castFromHand(player1, new HandOfHonor(), "{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ghost-Lit Stalker");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger can target the Kirin's controller")
    void canTargetItsController() {
        addInfernalKirin();
        harness.setHand(player1, List.of(
                new GhostLitRedeemer(), new GhostLitStalker(), new HandOfHonor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hand of Honor");
        harness.assertInGraveyard(player1, "Ghost-Lit Stalker");
    }

    @Test
    @DisplayName("A Spirit spell cast by an opponent does not trigger the Kirin")
    void opponentSpiritSpellDoesNotTrigger() {
        addInfernalKirin();
        harness.setHand(player1, List.of(new GhostLitStalker()));

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GhostLitRedeemer(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ghost-Lit Stalker");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void addInfernalKirin() {
        harness.addToBattlefield(player1, new InfernalKirin());
    }
}
