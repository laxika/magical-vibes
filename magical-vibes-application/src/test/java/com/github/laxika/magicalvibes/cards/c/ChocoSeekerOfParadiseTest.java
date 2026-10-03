package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChocoSeekerOfParadise.class, BirdsOfParadise.class, Forest.class,
        GrizzlyBears.class, Island.class, Unsummon.class})
class ChocoSeekerOfParadiseTest extends BaseCardTest {

    @Test
    @DisplayName("Counts only attacking Birds and puts the chosen land onto the battlefield tapped")
    void countsOnlyAttackingBirds() {
        Permanent choco = addReady(new ChocoSeekerOfParadise());
        addReady(new BirdsOfParadise());
        addReady(new GrizzlyBears());

        Card handCard = new GrizzlyBears();
        Card forest = new Forest();
        Card untouched = new Island();
        harness.setLibrary(player1, List.of(handCard, forest, untouched));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(firstChoice.allCards()).containsExactly(handCard, forest);
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(handCard);
        assertThat(findPermanent(player1, forest.getName()).isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(choco.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Puts any number of remaining lands onto the battlefield and the rest into the graveyard")
    void putsAnyNumberOfRemainingLandsOntoBattlefield() {
        addReady(new ChocoSeekerOfParadise());
        addReady(new BirdsOfParadise());
        addReady(new BirdsOfParadise());

        Card handCard = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(handCard, forest, island));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        PendingInteraction.LibraryRevealChoice landChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(landChoice.validCardIds()).containsExactly(forest.getId(), island.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(handCard);
        assertThat(findPermanent(player1, forest.getName()).isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island);
    }

    @Test
    @DisplayName("Does not trigger when only a non-Bird attacks")
    void doesNotTriggerForNonBirdAttackers() {
        addReady(new ChocoSeekerOfParadise());
        addReady(new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Uses the number of Birds that attacked even if a Bird leaves before resolution")
    void preservesAttackEventCountAfterBirdReturnsToHand() {
        addReady(new ChocoSeekerOfParadise());
        Permanent bird = addReady(new BirdsOfParadise());
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card untouched = new Island();
        harness.setLibrary(player1, List.of(first, second, untouched));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(List.of(0, 1));
        harness.castAndResolveInstant(player2, 0, bird.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(first, second);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    @Test
    @DisplayName("Can decline the hand card and put all looked-at lands onto the battlefield tapped")
    void declinesHandAndPutsAllLandsOntoBattlefield() {
        Permanent choco = addReady(new ChocoSeekerOfParadise());
        addReady(new BirdsOfParadise());
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), island.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest, island);
        assertThat(findPermanent(player1, forest.getName()).isTapped()).isTrue();
        assertThat(findPermanent(player1, island.getName()).isTapped()).isTrue();
        assertThat(choco.getEffectivePower()).isEqualTo(5);
        assertThat(choco.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can decline both choices and put all looked-at cards into the graveyard")
    void declinesBothChoices() {
        addReady(new ChocoSeekerOfParadise());
        addReady(new BirdsOfParadise());
        Card forest = new Forest();
        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bear));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, bear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest, bear);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Looks at the available cards when fewer remain than the number of attacking Birds")
    void handlesShortLibraryAndLandChosenForHand() {
        addReady(new ChocoSeekerOfParadise());
        addReady(new BirdsOfParadise());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, forest.getName())).isZero();
    }

    @Test
    @DisplayName("Resolves without a choice when the library is empty")
    void handlesEmptyLibrary() {
        addReady(new ChocoSeekerOfParadise());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers while Choco is not attacking and ignores opponents' land entries")
    void triggersForAnotherBirdAndOnlyOwnLands() {
        Permanent choco = addReady(new ChocoSeekerOfParadise());
        addReady(new BirdsOfParadise());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.enterBattlefieldAndReturn(player2, new Island());
        assertThat(gd.stack).isEmpty();
        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        resolveAllTriggers();

        assertThat(choco.getEffectivePower()).isEqualTo(4);
        assertThat(choco.getEffectiveToughness()).isEqualTo(5);
        assertThat(choco.isAttacking()).isFalse();
    }

    private Permanent addReady(Card card) {
        return addCreatureReady(player1, card);
    }
}
