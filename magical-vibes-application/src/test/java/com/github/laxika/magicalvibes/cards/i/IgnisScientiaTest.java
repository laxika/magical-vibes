package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IgnisScientia.class, Forest.class, GrizzlyBears.class})
class IgnisScientiaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put a revealed land onto the battlefield tapped")
    void etbMayPutLandTapped() {
        Card land = new Forest();
        List<Card> topCards = List.of(land, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new IgnisScientia()));
        addIgnisMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactlyElementsOf(topCards);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(permanentFor(land).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Exiling a creature card creates a Food token")
    void exilingCreatureCreatesFood() {
        Permanent ignis = addReadyIgnis();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        addIgnisMana();

        harness.activateAbility(player1, indexOf(ignis), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiling a noncreature card does not create a Food token")
    void exilingNoncreatureDoesNotCreateFood() {
        Permanent ignis = addReadyIgnis();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        addIgnisMana();

        harness.activateAbility(player1, indexOf(ignis), 0, null, land.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(land);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private Permanent addReadyIgnis() {
        return addCreatureReady(player1, new IgnisScientia());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Permanent permanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void addIgnisMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
