package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InformationDealer.class, Forest.class, Island.class, Mountain.class, Plains.class})
class InformationDealerTest extends BaseCardTest {

    @Test
    void countsWizardsOnBothBattlefieldsWhenAbilityResolves() {
        addCreatureReady(player1, new InformationDealer());
        Card topCard = new Forest();
        Card secondCard = new Island();
        Card thirdCard = new Mountain();
        Card fourthCard = new Plains();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard, fourthCard));

        harness.activateAbility(player1, 0, null, null);
        addCreatureReady(player2, new InformationDealer());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(secondCard, topCard, thirdCard, fourthCard);
    }

    @Test
    void looksAtOnlyOneCardWithOnlyTheSourceWizard() {
        addCreatureReady(player1, new InformationDealer());
        Card topCard = new Forest();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesZeroWhenSourceLeavesBeforeAbilityResolves() {
        Permanent informationDealer = addCreatureReady(player1, new InformationDealer());
        Card topCard = new Forest();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(informationDealer);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reordersEntireLibraryWhenThereAreMoreWizardsThanCards() {
        addCreatureReady(player1, new InformationDealer());
        addCreatureReady(player1, new InformationDealer());
        addCreatureReady(player2, new InformationDealer());
        Card topCard = new Forest();
        Card secondCard = new Island();
        Card opponentCard = new Mountain();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(topCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotRequireAnInteraction() {
        addCreatureReady(player1, new InformationDealer());
        addCreatureReady(player2, new InformationDealer());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
