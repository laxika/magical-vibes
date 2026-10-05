package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoreOverThePages.class, Forest.class, Island.class, QuilledWolf.class})
class PoreOverThePagesTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards, offers up to two lands to untap, then discards a card")
    void drawsUntapsAndDiscards() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Island());
        ownLand.tap();
        opposingLand.tap();

        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));
        harness.setHand(player1, List.of(new PoreOverThePages(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactly(ownLand.getId(), opposingLand.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownLand.getId(), opposingLand.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing zero lands still proceeds to discard")
    void canChooseZeroLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));
        harness.castFromHand(player1, new PoreOverThePages(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player1, "Pore Over the Pages");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May untap only one land even when two are available")
    void canChooseOneOfTwoLands() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new Forest());
        chosen.tap();
        unchosen.tap();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));
        harness.castFromHand(player1, new PoreOverThePages(), "{3}{U}{U}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.isTapped()).isFalse();
        assertThat(unchosen.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no lands, draws three cards and still discards a drawn card")
    void resolvesWithoutLands() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));
        harness.castFromHand(player1, new PoreOverThePages(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player1, "Pore Over the Pages");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Untap choice is limited to two lands and excludes nonland creatures")
    void onlyLandsAreEligibleAndOnlyTwoUntap() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        first.tap();
        second.tap();
        third.tap();
        creature.tap();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));
        harness.castFromHand(player1, new PoreOverThePages(), "{3}{U}{U}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), third.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
