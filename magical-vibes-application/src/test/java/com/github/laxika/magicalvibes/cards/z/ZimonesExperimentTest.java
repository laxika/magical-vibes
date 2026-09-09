package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void resolveExperiment() {
        harness.setHand(player1, List.of(new ZimonesExperiment()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
