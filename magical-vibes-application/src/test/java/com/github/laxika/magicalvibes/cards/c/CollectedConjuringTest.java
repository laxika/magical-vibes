package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.ExplosiveVegetation;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CollectedConjuring.class, Divination.class, ExplosiveVegetation.class,
        Forest.class, Ponder.class, Shock.class})
class CollectedConjuringTest extends BaseCardTest {

    @Test
    void offersOnlySorceriesWithManaValueAtMostThree() {
        Divination divination = new Divination();
        Ponder ponder = new Ponder();
        Shock shock = new Shock();
        ExplosiveVegetation expensiveSorcery = new ExplosiveVegetation();
        Forest forest = new Forest();
        Forest secondForest = new Forest();
        Forest leftover = new Forest();
        cast(List.of(divination, shock, expensiveSorcery, forest, ponder, secondForest, leftover));

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(divination.getId(), ponder.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(divination.getId(), shock.getId(), expensiveSorcery.getId(),
                        forest.getId(), ponder.getId(), secondForest.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(leftover);
    }

    @Test
    void castsUpToTwoEligibleSpellsForFreeAndBottomsTheRest() {
        Divination divination = new Divination();
        Ponder ponder = new Ponder();
        Shock shock = new Shock();
        Forest forest = new Forest();
        Forest secondForest = new Forest();
        Forest thirdForest = new Forest();
        Forest leftover = new Forest();
        cast(List.of(divination, ponder, shock, forest, secondForest, thirdForest, leftover));

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId(), ponder.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == divination);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == ponder);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(leftover, shock, forest, secondForest, thirdForest);
    }

    @Test
    void putsAllSixCardsOnTheBottomWhenNothingQualifies() {
        Shock shock = new Shock();
        ExplosiveVegetation expensiveSorcery = new ExplosiveVegetation();
        Forest forest = new Forest();
        Forest secondForest = new Forest();
        Forest thirdForest = new Forest();
        Forest fourthForest = new Forest();
        Forest leftover = new Forest();
        cast(List.of(shock, expensiveSorcery, forest, secondForest, thirdForest, fourthForest, leftover));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(shock, expensiveSorcery, forest, secondForest,
                        thirdForest, fourthForest, leftover);
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new CollectedConjuring()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
