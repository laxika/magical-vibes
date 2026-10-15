package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TripUp.class, GrizzlyBears.class, Island.class})
class TripUpTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner can put the nonland permanent on top of their library")
    void targetOwnerChoosesTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        castTripUp(target);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);

        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), topCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The target's owner can put the nonland permanent on the bottom of their library")
    void targetOwnerChoosesBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        castTripUp(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new TripUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Trip Up and draws a card")
    void cyclingDrawsACard() {
        TripUp tripUp = new TripUp();
        Card draw = new Island();
        harness.setHand(player1, List.of(tripUp));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tripUp);
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("The owner chooses the destination of a permanent controlled by another player")
    void ownerChoosesForBorrowedPermanent() {
        Card borrowed = new GrizzlyBears();
        borrowed.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, borrowed);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of());

        castTripUp(target);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, borrowed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trip Up does not resolve when its target has left the battlefield")
    void missingTargetDoesNotMoveAnything() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new TripUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
    }

    @Test
    @DisplayName("Cycling pays its discard cost before the draw resolves")
    void cyclingDiscardsBeforeResolution() {
        TripUp tripUp = new TripUp();
        Card draw = new Island();
        harness.setHand(player1, List.of(tripUp));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tripUp);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling requires two mana and does not discard when the cost cannot be paid")
    void cyclingRequiresTwoMana() {
        TripUp tripUp = new TripUp();
        harness.setHand(player1, List.of(tripUp));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tripUp);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(tripUp);
        assertThat(gd.stack).isEmpty();
    }

    private void castTripUp(Permanent target) {
        harness.setHand(player1, List.of(new TripUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
