package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostSanctifier;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KroxaAndKunoros.class, AlabasterHostSanctifier.class, Island.class})
class KroxaAndKunorosTest extends BaseCardTest {

    @Test
    @DisplayName("The enter trigger exiles exactly five cards and returns a targeted creature")
    void enterTriggerExilesFiveAndReanimatesCreature() {
        Card target = new AlabasterHostSanctifier();
        List<Card> cardsToExile = List.of(new Island(), new Island(), new Island(), new Island(), new Island());
        harness.castFromHand(player1, new KroxaAndKunoros(), "{3}{R}{W}{B}");
        harness.setGraveyard(player1, List.of(target, cardsToExile.get(0), cardsToExile.get(1), cardsToExile.get(2),
                cardsToExile.get(3), cardsToExile.get(4)));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, cardsToExile.stream().map(Card::getId).toList());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(cardsToExile);
        harness.assertOnBattlefield(player1, "Alabaster Host Sanctifier");
    }

    @Test
    @DisplayName("The attack trigger also exiles exactly five cards and returns a targeted creature")
    void attackTriggerExilesFiveAndReanimatesCreature() {
        Card target = new AlabasterHostSanctifier();
        List<Card> cardsToExile = List.of(new Island(), new Island(), new Island(), new Island(), new Island());
        addCreatureReady(player1, new KroxaAndKunoros());
        harness.setGraveyard(player1, List.of(target, cardsToExile.get(0), cardsToExile.get(1), cardsToExile.get(2),
                cardsToExile.get(3), cardsToExile.get(4)));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, cardsToExile.stream().map(Card::getId).toList());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(cardsToExile);
        harness.assertOnBattlefield(player1, "Alabaster Host Sanctifier");
    }

    @Test
    @DisplayName("Fewer than five cards cannot pay the optional exile")
    void fewerThanFiveCardsDoNotCreateFollowUp() {
        Card target = new AlabasterHostSanctifier();
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        harness.castFromHand(player1, new KroxaAndKunoros(), "{3}{R}{W}{B}");
        harness.setGraveyard(player1, List.of(target, first, second, third));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target, first, second, third);
    }

    @Test
    @DisplayName("Declining the exile leaves all graveyard cards untouched")
    void mayDeclineExileWithEnoughCards() {
        Card target = new AlabasterHostSanctifier();
        List<Card> graveyard = List.of(target, new Island(), new Island(), new Island(), new Island(), new Island());
        harness.setGraveyard(player1, graveyard);
        harness.castFromHand(player1, new KroxaAndKunoros(), "{3}{R}{W}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        harness.assertNotOnBattlefield(player1, "Alabaster Host Sanctifier");
    }

    @Test
    @DisplayName("Exactly five cards may be exiled even when no creature remains to target")
    void mayExileTheOnlyCreatureWithExactlyFiveCards() {
        List<Card> graveyard = List.of(new AlabasterHostSanctifier(), new Island(), new Island(),
                new Island(), new Island());
        harness.setGraveyard(player1, graveyard);
        harness.setGraveyard(player2, List.of(new AlabasterHostSanctifier()));
        harness.castFromHand(player1, new KroxaAndKunoros(), "{3}{R}{W}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Alabaster Host Sanctifier");
        harness.assertNotOnBattlefield(player1, "Alabaster Host Sanctifier");
        harness.assertNotOnBattlefield(player2, "Alabaster Host Sanctifier");
    }

    @Test
    @DisplayName("The reflexive trigger waits on the stack before returning its target")
    void reanimationIsASeparateTriggerAndDoesNotRetarget() {
        Card target = new AlabasterHostSanctifier();
        Card otherCreature = new AlabasterHostSanctifier();
        List<Card> cardsToExile = List.of(new Island(), new Island(), new Island(), new Island(), new Island());
        harness.setGraveyard(player1, List.of(target, otherCreature, cardsToExile.get(0), cardsToExile.get(1),
                cardsToExile.get(2), cardsToExile.get(3), cardsToExile.get(4)));
        harness.castFromHand(player1, new KroxaAndKunoros(), "{3}{R}{W}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, cardsToExile.stream().map(Card::getId).toList());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.assertNotOnBattlefield(player1, "Alabaster Host Sanctifier");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target, otherCreature);
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(otherCreature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alabaster Host Sanctifier");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(cardsToExile);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
