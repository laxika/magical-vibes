package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OathOfScholars.class)
class OathOfScholarsTest extends BaseCardTest {

    @Test
    void activePlayerMayDiscardTheirHandAndDrawThreeCards() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new OathOfScholars(), new OathOfScholars(), new OathOfScholars()));
        harness.setHand(player2, List.of(new OathOfScholars()));
        harness.setLibrary(player2, List.of(new OathOfScholars(), new OathOfScholars(), new OathOfScholars()));

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player2, "Oath of Scholars");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void decliningLeavesTheActivePlayersHandUntouched() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new OathOfScholars(), new OathOfScholars()));
        harness.setHand(player2, List.of(new OathOfScholars()));
        harness.setLibrary(player2, List.of(new OathOfScholars(), new OathOfScholars(), new OathOfScholars()));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Oath of Scholars");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void targetMustStillHaveMoreCardsWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new OathOfScholars(), new OathOfScholars()));
        harness.setHand(player2, List.of(new OathOfScholars()));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.setHand(player2, List.of(new OathOfScholars(), new OathOfScholars()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerWhenNoOpponentHasMoreCardsInHand() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new OathOfScholars(), new OathOfScholars()));
        harness.setHand(player2, List.of(new OathOfScholars(), new OathOfScholars()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void activePlayerMayDiscardTheirHandOnTheirOwnUpkeep() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new OathOfScholars()));
        harness.setHand(player2, List.of(new OathOfScholars(), new OathOfScholars()));
        harness.setLibrary(player1, List.of(new OathOfScholars(), new OathOfScholars(), new OathOfScholars()));

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Oath of Scholars");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void emptyHandStillAllowsDrawingThreeCards() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new OathOfScholars()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new OathOfScholars(), new OathOfScholars(), new OathOfScholars()));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void discardsTheEntireHandBeforeDrawingExactlyThreeCards() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new OathOfScholars(), new OathOfScholars(), new OathOfScholars()));
        Card firstDiscard = new OathOfScholars();
        Card secondDiscard = new OathOfScholars();
        harness.setHand(player2, List.of(firstDiscard, secondDiscard));
        Card firstDraw = new OathOfScholars();
        Card secondDraw = new OathOfScholars();
        Card thirdDraw = new OathOfScholars();
        Card remaining = new OathOfScholars();
        harness.setLibrary(player2, List.of(firstDraw, secondDraw, thirdDraw, remaining));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void abilityDoesNotResolveWhenTargetDiscardsDownToFewerCards() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new OathOfScholars(), new OathOfScholars()));
        Card originalHandCard = new OathOfScholars();
        harness.setHand(player2, List.of(originalHandCard));
        harness.setLibrary(player2, List.of(new OathOfScholars(), new OathOfScholars(), new OathOfScholars()));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(originalHandCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }
}
