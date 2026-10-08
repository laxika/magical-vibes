package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZimonesExperiment.class, Forest.class, GrizzlyBears.class, LlanowarElves.class,
        Shock.class, DryadArbor.class, GrafdiggersCage.class})
class ZimonesExperimentTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two creature and/or land cards from the top five")
    void offersEligibleCards() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card elves = new LlanowarElves();
        Card secondForest = new Forest();
        setLibrary(forest, bears, shock, elves, secondForest);

        resolveExperiment();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                forest.getId(), bears.getId(), elves.getId(), secondForest.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.minCount()).isZero();
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Puts selected lands onto the battlefield tapped and creatures into hand")
    void routesSelectedCardsByType() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card elves = new LlanowarElves();
        Card secondForest = new Forest();
        setLibrary(forest, bears, shock, elves, secondForest);

        resolveExperiment();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), bears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears).doesNotContain(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(shock, elves, secondForest);
    }

    @Test
    @DisplayName("Allows two creatures to be selected")
    void allowsTwoCreatures() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        Card forest = new Forest();
        Card secondForest = new Forest();
        setLibrary(bears, elves, shock, forest, secondForest);

        resolveExperiment();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears, elves);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == bears || permanent.getCard() == elves);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(shock, forest, secondForest);
    }

    @Test
    @DisplayName("Choosing no cards puts all looked-at cards on the bottom randomly")
    void mayChooseNoCards() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card elves = new LlanowarElves();
        Card secondForest = new Forest();
        setLibrary(forest, bears, shock, elves, secondForest);

        resolveExperiment();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest, bears, elves, secondForest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest || permanent.getCard() == bears
                        || permanent.getCard() == elves || permanent.getCard() == secondForest);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, bears, shock, elves, secondForest);
    }

    @Test
    void allowsTwoLands() {
        Card first = new Forest();
        Card second = new Forest();
        setLibrary(first, second);

        resolveExperiment();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == first && p.isTapped())
                .anyMatch(p -> p.getCard() == second && p.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void allowsOneCardFromShortLibrary() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        setLibrary(bears, shock);

        resolveExperiment();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears).doesNotContain(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void leavesCardsBelowTopFiveInOrder() {
        Card first = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card elves = new LlanowarElves();
        Card second = new Forest();
        Card sixth = new GrizzlyBears();
        Card seventh = new Forest();
        setLibrary(first, bears, shock, elves, second, sixth, seventh);

        resolveExperiment();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).doesNotContain(sixth.getId(), seventh.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrder(bears, shock, elves, second);
    }

    @Test
    void resolvesWithoutEligibleCards() {
        Card first = new Shock();
        Card second = new Shock();
        setLibrary(first, second);

        resolveExperiment();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        harness.assertInGraveyard(player1, "Zimone's Experiment");
    }

    @Test
    void resolvesWithEmptyLibrary() {
        setLibrary();

        resolveExperiment();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Zimone's Experiment");
    }

    @Test
    void creatureLandEntersTappedAndDoesNotAlsoGoToHand() {
        Card arbor = new DryadArbor();
        setLibrary(arbor);

        resolveExperiment();
        harness.handleMultipleCardsChosen(player1, List.of(arbor.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == arbor && p.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(arbor);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(arbor);
    }

    @Test
    void blockedCreatureLandGoesToHandWithoutShufflingLibrary() {
        Card arbor = new DryadArbor();
        Card first = new Shock();
        Card second = new Shock();
        Card third = new Shock();
        Card fourth = new Shock();
        Card sixth = new Forest();
        Card seventh = new GrizzlyBears();
        setLibrary(arbor, first, second, third, fourth, sixth, seventh);
        harness.addToBattlefield(player2, new GrafdiggersCage());

        resolveExperiment();
        harness.handleMultipleCardsChosen(player1, List.of(arbor.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(arbor);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == arbor);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrder(first, second, third, fourth);
    }

    private void resolveExperiment() {
        harness.castFromHand(player1, new ZimonesExperiment(), "{3}{G}");
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
