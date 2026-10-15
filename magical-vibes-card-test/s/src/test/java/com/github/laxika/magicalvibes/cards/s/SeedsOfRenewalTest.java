package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedsOfRenewal.class, Forest.class, GrizzlyBears.class, Shock.class})
class SeedsOfRenewalTest extends BaseCardTest {

    @Test
    void returnsUpToTwoCardsAndExilesSeedsOfRenewal() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(forest, bears, shock));
        harness.setHand(player1, List.of(new SeedsOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId(), bears.getId(), shock.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seeds of Renewal"));
        harness.assertNotInGraveyard(player1, "Seeds of Renewal");
    }

    @Test
    void canReturnNoCardsAndStillExilesSeedsOfRenewal() {
        harness.setHand(player1, List.of(new SeedsOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seeds of Renewal"));
        harness.assertNotInGraveyard(player1, "Seeds of Renewal");
    }
    @Test
    void canChooseZeroCardsFromNonemptyGraveyard() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new SeedsOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seeds of Renewal"));
    }

    @Test
    void canReturnJustOneCardWithoutTargetingOpponentsGraveyard() {
        Card forest = new Forest();
        Card opponentsForest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setGraveyard(player2, List.of(opponentsForest));
        harness.setHand(player1, List.of(new SeedsOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seeds of Renewal"));
    }

    @Test
    void doesNotExileItselfWhenAllChosenTargetsLeaveGraveyard() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new SeedsOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Seeds of Renewal");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Seeds of Renewal"));
    }

    @Test
    void returnsRemainingLegalTargetAndExilesItself() {
        Card forest = new Forest();
        Card otherForest = new Forest();
        harness.setGraveyard(player1, List.of(forest, otherForest));
        harness.setHand(player1, List.of(new SeedsOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), otherForest.getId()));
        harness.setGraveyard(player1, List.of(otherForest));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(otherForest).doesNotContain(forest);
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seeds of Renewal"));
    }
}
