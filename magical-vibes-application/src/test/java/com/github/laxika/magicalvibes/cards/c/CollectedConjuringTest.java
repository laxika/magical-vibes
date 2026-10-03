package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.b.BoneSplinters;
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
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DreadReturn;




@CardUsed({CollectedConjuring.class, Divination.class, ExplosiveVegetation.class,
        Forest.class, Ponder.class, Shock.class, BoneSplinters.class, GrizzlyBears.class})
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

    @Test
    void permitsCastingSorceryWithPayableMandatoryAdditionalCost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        BoneSplinters splinters = new BoneSplinters();
        cast(List.of(splinters, new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        harness.handleMultipleCardsChosen(player1, List.of(splinters.getId()));

        // Casting must continue with the target and sacrifice choices instead of skipping the spell.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(splinters);
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new CollectedConjuring(), "{2}{U}{R}");
        harness.passBothPriorities();
    }
}

@CardUsed({CollectedConjuring.class, CrashingFootfalls.class, DreadReturn.class,
        Forest.class, GrizzlyBears.class, Shock.class})
class Mh1CollectedConjuringTest extends BaseCardTest {

    @Test
    void offersUpToTwoSorceriesWithManaValueThreeOrLess() {
        CrashingFootfalls first = new CrashingFootfalls();
        CrashingFootfalls second = new CrashingFootfalls();
        Forest leftover = new Forest();
        List<Card> library = List.of(
                first, new Shock(), second, new GrizzlyBears(), new Forest(), new DreadReturn(), leftover);
        cast(library);

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        library.get(1).getId(), library.get(3).getId(), library.get(4).getId(),
                        library.get(5).getId(), library.get(6).getId());
        assertThat(gd.playerDecks.get(player1.getId())).contains(leftover);
    }

    @Test
    void putsAllCardsOnTheBottomWhenNoEligibleSorceryIsFound() {
        List<Card> library = List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Forest(), new Forest(), new Forest(),
                new Forest());
        cast(library);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    void mayDeclineAllEligibleSpellsAndPreservesUnexiledTopCards() {
        CrashingFootfalls first = new CrashingFootfalls();
        CrashingFootfalls second = new CrashingFootfalls();
        Forest top = new Forest();
        Forest next = new Forest();
        List<Card> library = List.of(first, second, new Forest(), new Forest(), new Forest(),
                new Forest(), top, next);
        cast(library);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(top, next);
    }

    @Test
    void mayCastOnlyOneSpellFromAShortLibrary() {
        CrashingFootfalls chosen = new CrashingFootfalls();
        CrashingFootfalls declined = new CrashingFootfalls();
        Forest forest = new Forest();
        cast(List.of(chosen, declined, forest));

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(chosen.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(declined, forest);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Crashing Footfalls");
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new CollectedConjuring(), "{2}{U}{R}");
        harness.passBothPriorities();
    }
}
