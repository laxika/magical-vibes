package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WispdrinkerVampire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntrudeOnTheMind.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class,
        WispdrinkerVampire.class, GarruksPackleader.class})
class IntrudeOnTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent chooses the hand pile and the Thopter gets counters for the graveyard pile")
    void opponentChoosesPileAndCreatesCounteredThopter() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card mountain = new Mountain();
        IntrudeOnTheMind spell = new IntrudeOnTheMind();
        harness.setLibrary(player1, List.of(island, forest, swamp, plains, mountain));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation).isNotNull();
        assertThat(separation.playerId()).isEqualTo(player1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(island.getId(), forest.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> !card.getId().equals(spell.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(swamp.getId(), plains.getId(), mountain.getId());

        List<Permanent> tokens = findPermanents(player1, "Thopter");
        assertThat(tokens).hasSize(1);
        Permanent thopter = tokens.getFirst();
        assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
        assertThat(thopter.getCard().getPower()).isZero();
        assertThat(thopter.getCard().getToughness()).isZero();
        assertThat(thopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(thopter.getCard().getColor()).isNull();
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    void opponentCanChooseSecondPileAndOnlyTopFiveCardsAreRevealed() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card mountain = new Mountain();
        Card sixth = new Island();
        harness.setLibrary(player1, List.of(island, forest, swamp, plains, mountain, sixth));
        castSpell();

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(swamp, plains, mountain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth);
        assertThat(findPermanent(player1, "Thopter").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void choosingEmptyPilePutsAllRevealedCardsIntoGraveyard() {
        List<Card> cards = List.of(new Island(), new Forest(), new Swamp(), new Plains(), new Mountain());
        harness.setLibrary(player1, cards);
        castSpell();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(cards);
        assertThat(findPermanent(player1, "Thopter").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
    }

    @Test
    void choosingAllCardsCreatesZeroToughnessThopterThatDiesAfterTriggering() {
        harness.addToBattlefield(player1, new WispdrinkerVampire());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Card island = new Island();
        harness.setLibrary(player1, List.of(island));
        castSpell();

        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        harness.assertNotOnBattlefield(player1, "Thopter");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void shortLibraryUsesOnlyCardsActuallyPutIntoGraveyardForCounters() {
        Card island = new Island();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(island, forest));
        castSpell();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Thopter").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void emptyLibraryStillCreatesThopterAndTriggersCreatureEntryAbility() {
        harness.addToBattlefield(player1, new WispdrinkerVampire());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of());
        castSpell();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thopter");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void thopterEntersWithZeroPowerBeforeReceivingCounters() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Swamp(), new Plains(), new Mountain()));
        castSpell();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(findPermanent(player1, "Thopter").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void pileChoicePromptDescribesPuttingChosenCardsIntoControllersHand() {
        harness.setLibrary(player1, List.of(new Island()));
        castSpell();
        harness.handleMultipleCardsChosen(player1, List.of());

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.description()).contains("hand").doesNotContain("battlefield");
    }

    private void castSpell() {
        harness.setHand(player1, List.of(new IntrudeOnTheMind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
    }
}
