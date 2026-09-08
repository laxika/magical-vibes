package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hypnox.class, GrizzlyBears.class, LlanowarElves.class, BeaconOfUnrest.class})
class HypnoxTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand, Hypnox exiles all cards from a target opponent's hand")
    void castFromHandExilesTargetOpponentsHand() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        castHypnoxWithTargetHand(List.of(first, second));

        Permanent hypnox = findPermanent(player1, "Hypnox");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, hypnox.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("When Hypnox leaves the battlefield, its exiled cards return to their owners' hands")
    void exiledCardsReturnWhenHypnoxLeaves() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        castHypnoxWithTargetHand(List.of(first, second));
        Permanent hypnox = findPermanent(player1, "Hypnox");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hypnox));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.exiledCards)
                .noneMatch(entry -> hypnox.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("Entering from the graveyard does not trigger Hypnox's hand exile")
    void enteringFromGraveyardDoesNotExileHand() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        harness.setGraveyard(player1, List.of(new Hypnox()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hypnox");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card() == handCard);
    }

    private void castHypnoxWithTargetHand(List<Card> targetHand) {
        harness.setHand(player2, new ArrayList<>(targetHand));
        harness.setHand(player1, List.of(new Hypnox()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
