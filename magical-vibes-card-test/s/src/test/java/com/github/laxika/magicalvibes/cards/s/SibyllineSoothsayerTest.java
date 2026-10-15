package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SibyllineSoothsayer.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class})
class SibyllineSoothsayerTest extends BaseCardTest {

    @Test
    void exilesFirstNonlandWithManaValueAtLeastThreeAndSuspendsIt() {
        Forest land = new Forest();
        GrizzlyBears belowThreshold = new GrizzlyBears();
        CounselOfTheSoratami qualifyingCard = new CounselOfTheSoratami();
        GrizzlyBears afterQualifyingCard = new GrizzlyBears();
        setUpSoothsayer(List.of(land, belowThreshold, qualifyingCard, afterQualifyingCard));

        resolveEnterTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(qualifyingCard);
        assertThat(gd.exiledCardTimeCounters).containsEntry(qualifyingCard.getId(), 3);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, belowThreshold, afterQualifyingCard);
    }

    @Test
    void bottomsAllRevealedCardsWhenNoQualifyingCardExists() {
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        setUpSoothsayer(List.of(land, nonland));

        resolveEnterTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(nonland.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, nonland);
    }

    @Test
    void preservesUnrevealedOrderAndPlacesSkippedCardsAtTheBottom() {
        Forest skippedLand = new Forest();
        GrizzlyBears skippedCreature = new GrizzlyBears();
        SibyllineSoothsayer qualifyingCard = new SibyllineSoothsayer();
        Forest unrevealedFirst = new Forest();
        GrizzlyBears unrevealedSecond = new GrizzlyBears();
        setUpSoothsayer(List.of(skippedLand, skippedCreature, qualifyingCard,
                unrevealedFirst, unrevealedSecond));

        resolveEnterTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(qualifyingCard);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.subList(0, 2)).containsExactly(unrevealedFirst, unrevealedSecond);
        assertThat(library.subList(2, 4)).containsExactlyInAnyOrder(skippedLand, skippedCreature);
    }

    @Test
    void emptyLibraryDoesNotExileAnything() {
        setUpSoothsayer(List.of());

        resolveEnterTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exiledCardTimeCounters).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void grantedSuspendRemovesCountersOnlyOnOwnersUpkeepsAndOffersCasting() {
        SibyllineSoothsayer qualifyingCard = new SibyllineSoothsayer();
        setUpSoothsayer(List.of(qualifyingCard, new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        resolveEnterTrigger();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gd.exiledCardTimeCounters).containsEntry(qualifyingCard.getId(), 3);

        for (int remaining = 2; remaining >= 1; remaining--) {
            harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
            harness.passBothPriorities();
            assertThat(gd.exiledCardTimeCounters).containsEntry(qualifyingCard.getId(), remaining);
            harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
            assertThat(gd.exiledCardTimeCounters).containsEntry(qualifyingCard.getId(), remaining);
        }

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(qualifyingCard.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(qualifyingCard);
    }

    private void setUpSoothsayer(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SibyllineSoothsayer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
