package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.h.HollowMarauder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreestriderLookout.class, Forest.class, Shock.class, HollowMarauder.class})
class FreestriderLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Crime trigger may put a land from the top five onto the battlefield tapped")
    void crimeTriggerPutsChosenLandOntoBattlefieldTapped() {
        FreestriderLookout lookout = new FreestriderLookout();
        Forest forest = new Forest();
        setLibrary(new Shock(), forest, new Shock(), new Shock(), new Shock());
        harness.addToBattlefield(player1, lookout);

        commitCrime();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent enteredForest = findPermanent(player1, "Forest");
        assertThat(enteredForest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Crime trigger puts all five cards on the bottom when no land is chosen")
    void crimeTriggerDoesNothingWithoutLand() {
        setLibrary(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.addToBattlefield(player1, new FreestriderLookout());

        commitCrime();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        setLibrary(new Forest(), new Forest(), new Shock(), new Shock(), new Shock());
        harness.addToBattlefield(player1, new FreestriderLookout());

        commitCrime();
        harness.handleMultipleCardsChosen(player1,
                List.of(((PendingInteraction.LibraryRevealChoice) gd.interaction.activeInteraction()).validCardIds().getFirst()));

        commitCrime();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining an available land bottoms all five cards and still uses the turn's trigger")
    void canDeclineLandWithoutTriggeringAgain() {
        Forest forest = new Forest();
        Shock sixthCard = new Shock();
        List<Card> topFive = List.of(forest, new Shock(), new Shock(), new Shock(), new Shock());
        setLibrary(topFive.get(0), topFive.get(1), topFive.get(2), topFive.get(3), topFive.get(4), sixthCard);
        harness.addToBattlefield(player1, new FreestriderLookout());

        commitCrime();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(countPermanents(player1, "Forest")).isZero();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(sixthCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(topFive);

        commitCrime();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than five cards still offers its land")
    void handlesShortLibrary() {
        Forest forest = new Forest();
        Shock otherCard = new Shock();
        setLibrary(otherCard, forest);
        harness.addToBattlefield(player1, new FreestriderLookout());

        commitCrime();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
    }

    @Test
    @DisplayName("An empty library completes the crime trigger without a choice")
    void handlesEmptyLibrary() {
        setLibrary();
        harness.addToBattlefield(player1, new FreestriderLookout());

        commitCrime();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting yourself does not trigger Lookout or use its once-per-turn trigger")
    void targetingSelfDoesNotCommitCrime() {
        Forest forest = new Forest();
        Shock otherCard = new Shock();
        setLibrary(forest, otherCard);
        harness.addToBattlefield(player1, new FreestriderLookout());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, otherCard);

        commitCrime();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Targeting an opponent's creature commits a crime")
    void targetingOpposingCreatureCommitsCrime() {
        Forest forest = new Forest();
        setLibrary(forest, new Shock());
        harness.addToBattlefield(player1, new FreestriderLookout());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FreestriderLookout());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(countPermanents(player2, "Forest")).isZero();
    }

    @Test
    @DisplayName("Lookout can trigger again during the opponent's next turn")
    void triggersAgainOnOpponentsTurn() {
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        setLibrary(firstLand, secondLand, new Shock(), new Shock(), new Shock(), new Shock());
        harness.addToBattlefield(player1, new FreestriderLookout());

        commitCrime();
        harness.handleMultipleCardsChosen(player1, List.of(firstLand.getId()));
        harness.passUntil(player2, TurnStep.UPKEEP);

        commitCrime();
        harness.handleMultipleCardsChosen(player1, List.of(secondLand.getId()));

        assertThat(findPermanents(player1, "Forest")).hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("An enters-the-battlefield ability targeting an opponent triggers Lookout")
    void targetedTriggeredAbilityCommitsCrime() {
        Forest forest = new Forest();
        setLibrary(forest, new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.addToBattlefield(player1, new FreestriderLookout());
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, List.of(player2.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    private void commitCrime() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
