package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighwayRobbery.class, Forest.class, GrizzlyBears.class})
class HighwayRobberyTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card draws two cards")
    void discardingDrawsTwoCards() {
        GrizzlyBears discarded = new GrizzlyBears();
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new HighwayRobbery(), discarded)));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        addMana();

        castAndAccept();
        harness.handleListChoice(player1, "Discard a card");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
    }

    @Test
    @DisplayName("Sacrificing a chosen land draws two cards")
    void sacrificingLandDrawsTwoCards() {
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HighwayRobbery()));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        addMana();

        castAndAccept();
        harness.handleListChoice(player1, "Sacrifice a land");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(secondLand.getId()));

        assertThat(secondLand).isNotIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(firstLand).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
    }

    @Test
    @DisplayName("Declining the optional action does nothing")
    void declineDoesNothing() {
        GrizzlyBears inHand = new GrizzlyBears();
        Forest onTop = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new HighwayRobbery(), inHand)));
        harness.setLibrary(player1, List.of(onTop));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(inHand);
    }

    private void castAndAccept() {
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
