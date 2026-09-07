package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShimmerMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshePrincessOfDalmasca.class, ShimmerMyr.class, GrizzlyBears.class, Forest.class})
class AshePrincessOfDalmascaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers an artifact among the top five cards")
    void attackingOffersArtifactAmongTopFive() {
        Card artifact = new ShimmerMyr();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), artifact, new Forest(), new GrizzlyBears(), new Forest()));

        declareAttack();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(5);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing an artifact puts it into hand and bottoms the rest")
    void choosingArtifactPutsItIntoHand() {
        Card artifact = new ShimmerMyr();
        List<Card> otherCards = List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest());
        harness.setLibrary(player1, List.of(artifact, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3)));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(otherCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no artifact among the top five, all five cards are bottomed")
    void noArtifactBottomsAllFive() {
        List<Card> library = List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears());
        harness.setLibrary(player1, library);

        declareAttack();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    private void declareAttack() {
        Permanent ashe = harness.addToBattlefieldAndReturn(player1, new AshePrincessOfDalmasca());
        ashe.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();
    }
}
