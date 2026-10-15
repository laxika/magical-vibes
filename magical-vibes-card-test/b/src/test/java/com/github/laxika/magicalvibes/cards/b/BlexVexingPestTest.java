package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlexVexingPest.class, Forest.class, GrizzlyBears.class, HillGiant.class, WrathOfGod.class})
class BlexVexingPestTest extends BaseCardTest {

    @Test
    void boostsOtherControlledPestsAndNotOpponents() {
        Permanent blex = addCreatureReady(player1, new BlexVexingPest());
        Permanent pest = addCreatureReady(player1, new GrizzlyBears());
        TestCards.mutableCard(pest).setSubtypes(List.of(CardSubtype.PEST));
        Permanent other = addCreatureReady(player1, new HillGiant());
        Permanent opponentPest = addCreatureReady(player2, new GrizzlyBears());
        TestCards.mutableCard(opponentPest).setSubtypes(List.of(CardSubtype.PEST));

        assertThat(gqs.getEffectivePower(gd, blex)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blex)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentPest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentPest)).isEqualTo(2);
    }

    @Test
    void gainsFourLifeWhenItDies() {
        harness.addToBattlefield(player1, new BlexVexingPest());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 10);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
    }

    @Test
    void searchForBlexPutsChosenCardsInHandAndLosesThreeLifePerCard() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new HillGiant();
        Card fourth = new Forest();
        Card fifth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new BlexVexingPest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third, fourth, fifth);
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    void boostsEachOtherListedSubtypeOnlyOnce() {
        harness.addToBattlefield(player1, new BlexVexingPest());
        for (CardSubtype subtype : List.of(CardSubtype.BAT, CardSubtype.INSECT, CardSubtype.SNAKE, CardSubtype.SPIDER)) {
            Permanent creature = addCreatureReady(player1, new GrizzlyBears());
            TestCards.mutableCard(creature).setSubtypes(List.of(subtype));

            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        }
        Permanent multipleTypes = addCreatureReady(player1, new GrizzlyBears());
        TestCards.mutableCard(multipleTypes).setSubtypes(List.of(CardSubtype.PEST, CardSubtype.BAT, CardSubtype.INSECT));

        assertThat(gqs.getEffectivePower(gd, multipleTypes)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, multipleTypes)).isEqualTo(3);
    }

    @Test
    void deathTriggerGainsLifeForBlexControllerRatherThanWrathCaster() {
        harness.addToBattlefield(player2, new BlexVexingPest());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 14);
    }

    @Test
    void castsCreatureFaceUsingItsGreenManaCost() {
        harness.setHand(player1, List.of(new BlexVexingPest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Blex, Vexing Pest");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void searchCanChooseNoCardsWithoutLosingLife() {
        List<Card> cards = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new BlexVexingPest()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(cards);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Blex, Vexing Pest");
    }

    @Test
    void searchCanTakeAllFiveCardsAndLeavesTheSixthInLibrary() {
        List<Card> cards = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        Card sixth = new Forest();
        harness.setLibrary(player1, List.of(cards.get(0), cards.get(1), cards.get(2), cards.get(3), cards.get(4), sixth));
        harness.setHand(player1, List.of(new BlexVexingPest()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, cards.stream().map(Card::getId).toList());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContainAnyElementsOf(cards);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth);
        harness.assertLife(player1, 5);
    }

    @Test
    void searchWithShortLibraryTakesAllAvailableCards() {
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new BlexVexingPest()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 17);
    }

    @Test
    void searchWithEmptyLibraryDoesNotLoseLifeOrLeaveAChoicePending() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BlexVexingPest()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }
}
