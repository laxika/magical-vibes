package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DivineVerdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantasmalExtraction.class, Forest.class, GrizzlyBears.class, PsychicIntrusion.class,
        DivineVerdict.class})
class PhantasmalExtractionTest extends BaseCardTest {

    @Test
    void onlyAllowsEligibleHandCardsAndExilesTheChosenCard() {
        Card land = new Forest();
        Card eligibleCard = new GrizzlyBears();
        Card tooExpensiveCard = new PsychicIntrusion();
        harness.setHand(player1, List.of(land, eligibleCard, tooExpensiveCard));

        castAsNonStartingPlayer();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(land.getId())))
                .hasMessageContaining("valid nonland card");
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(tooExpensiveCard.getId())))
                .hasMessageContaining("valid nonland card");

        harness.handleMultipleCardsChosen(player2, List.of(eligibleCard.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligibleCard);
    }

    @Test
    void canExileAnyCardFromTheTargetGraveyard() {
        Card graveyardLand = new Forest();
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(graveyardLand));

        castAsNonStartingPlayer();
        harness.handleMultipleCardsChosen(player2, List.of(graveyardLand.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardLand);
    }

    @Test
    void nonStartingPlayerGetsCostReduction() {
        forceNonStartingPlayer();
        harness.setHand(player2, List.of(new PhantasmalExtraction()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castSorcery(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void startingPlayerDoesNotGetCostReduction() {
        harness.setHand(player1, List.of(new PhantasmalExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revealsOnlyEligibleHandCardsToBothPlayersAndIncludesManaValueFour() {
        Card eligibleCard = new DivineVerdict();
        Card land = new Forest();
        Card expensiveCard = new PsychicIntrusion();
        harness.setHand(player1, List.of(eligibleCard, land, expensiveCard));

        castAsNonStartingPlayer();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .singleElement().asString()
                .contains(eligibleCard.getId().toString())
                .doesNotContain(land.getId().toString(), expensiveCard.getId().toString());
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND"))
                .singleElement().asString()
                .contains(eligibleCard.getId().toString())
                .doesNotContain(land.getId().toString(), expensiveCard.getId().toString());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player2, List.of(eligibleCard.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligibleCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, expensiveCard);
    }

    @Test
    void canChooseExpensiveGraveyardCardInsteadOfEligibleHandCard() {
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new PsychicIntrusion();
        harness.setHand(player1, List.of(handCard));
        harness.setGraveyard(player1, List.of(graveyardCard));

        castAsNonStartingPlayer();
        harness.handleMultipleCardsChosen(player2, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        forceNonStartingPlayer();
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castFromExile(player2, graveyardCard.getId(), player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvesWithoutChoiceWhenNeitherZoneContainsAnEligibleCard() {
        Card land = new Forest();
        Card expensiveCard = new PsychicIntrusion();
        harness.setHand(player1, List.of(land, expensiveCard));
        harness.setGraveyard(player1, List.of());

        castAsNonStartingPlayer();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, expensiveCard);
    }

    @Test
    void cannotTargetTheCaster() {
        forceNonStartingPlayer();
        harness.setHand(player2, List.of(new PhantasmalExtraction()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void costReductionDoesNotRemoveTheBlackManaRequirement() {
        forceNonStartingPlayer();
        harness.setHand(player2, List.of(new PhantasmalExtraction()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void startingPlayerCanCastForTheFullCost() {
        Card opponentCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new PhantasmalExtraction()));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
    }

    private void castAsNonStartingPlayer() {
        forceNonStartingPlayer();
        harness.setHand(player2, List.of(new PhantasmalExtraction()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
    }

    private void forceNonStartingPlayer() {
        harness.forceActivePlayer(player2);
        gd.startingPlayerId = player1.getId();
    }
}
