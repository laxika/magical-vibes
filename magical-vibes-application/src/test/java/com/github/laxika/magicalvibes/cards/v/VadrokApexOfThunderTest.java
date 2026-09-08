package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VadrokApexOfThunder.class, Concentrate.class, Divination.class, Forest.class, GrizzlyBears.class})
class VadrokApexOfThunderTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating offers target noncreature cards with mana value 3 or less")
    void mutationOffersMatchingNoncreatureCards() {
        Permanent vadrok = addCreatureReady(player1, new VadrokApexOfThunder());
        Card divination = new Divination();
        Card forest = new Forest();
        Card concentrate = new Concentrate();
        Card grizzlyBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(divination, forest, concentrate, grizzlyBears));

        triggerMutation(vadrok);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(divination.getId(), forest.getId());
    }

    @Test
    @DisplayName("Mutating may cast the selected noncreature card without paying its mana cost")
    void mutationMayCastSelectedCardForFree() {
        Permanent vadrok = addCreatureReady(player1, new VadrokApexOfThunder());
        Card divination = new Divination();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(divination));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        triggerMutation(vadrok);
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(firstDraw.getId(), secondDraw.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(divination.getId());
    }

    @Test
    @DisplayName("Declining the cast leaves the selected card in the graveyard")
    void decliningCastLeavesCardInGraveyard() {
        Permanent vadrok = addCreatureReady(player1, new VadrokApexOfThunder());
        Card divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        triggerMutation(vadrok);
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(divination.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A targeted land cannot be cast")
    void targetingLandDoesNotOfferACast() {
        Permanent vadrok = addCreatureReady(player1, new VadrokApexOfThunder());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        triggerMutation(vadrok);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(forest.getId());
    }

    private void triggerMutation(Permanent vadrok) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, vadrok, List.of(vadrok.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSelfTriggeredAbilityTarget(gd));
    }
}
