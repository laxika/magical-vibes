package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilvergillPeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped draws a card, then prompts for a discard")
    void becomingTappedDrawsThenDiscards() {
        Permanent peddler = harness.addToBattlefieldAndReturn(player1, new SilvergillPeddler());
        GrizzlyBears bears = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(bears));
        setDeck(player1, List.of(drawn));

        tap(peddler);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isSameAs(drawn);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger Silvergill Peddler")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SilvergillPeddler());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(bears);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping an opponent's creature does not trigger Silvergill Peddler")
    void tappingOpponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SilvergillPeddler());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(bears);

        assertThat(gd.stack).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
