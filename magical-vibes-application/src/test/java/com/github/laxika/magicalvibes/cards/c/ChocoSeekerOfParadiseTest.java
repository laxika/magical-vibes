package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChocoSeekerOfParadise.class, BirdsOfParadise.class, Forest.class,
        GrizzlyBears.class, Island.class})
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
        assertThat(permanentFor(forest).isTapped()).isTrue();
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
        assertThat(permanentFor(forest).isTapped()).isTrue();
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

    private Permanent addReady(Card card) {
        return addCreatureReady(player1, card);
    }

    private Permanent permanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
