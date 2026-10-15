package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({ThreeTreeBattalion.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class ThreeTreeBattalionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a qualifying creature onto the battlefield and creates a 1/1 duplicate")
    void putsCreatureAndCreatesDuplicate() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears, new HillGiant(), new Shock());

        castBattalion();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        List<Permanent> bearsOnBattlefield = findPermanents(player1, "Grizzly Bears");
        assertThat(bearsOnBattlefield).hasSize(2);
        assertThat(bearsOnBattlefield).anySatisfy(permanent -> {
            assertThat(permanent.getCard()).isSameAs(bears);
            assertThat(permanent.getEffectivePower()).isEqualTo(2);
            assertThat(permanent.getEffectiveToughness()).isEqualTo(2);
        });
        assertThat(bearsOnBattlefield).anySatisfy(permanent -> {
            assertThat(permanent.getCard()).isNotSameAs(bears);
            assertThat(permanent.getEffectivePower()).isEqualTo(1);
            assertThat(permanent.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Can decline to put a qualifying creature onto the battlefield")
    void mayDeclineCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears, new Shock(), new HillGiant());

        castBattalion();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The conjured duplicate remains a creature card in the graveyard after dying")
    void duplicateRemainsInGraveyardAfterDying() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears);
        castBattalion();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent duplicate = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard() != bears)
                .findFirst().orElseThrow();
        Card duplicateCard = duplicate.getCard();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, duplicate.getId());
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(duplicateCard);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Only the top six cards are considered and the rest go below untouched cards")
    void looksAtOnlySixCardsAndBottomsRemainder() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock untouched = new Shock();
        List<Card> rest = List.of(new Shock(), new HillGiant(), new Shock(), new HillGiant(), new Shock());
        harness.setLibrary(player1, List.of(bears, rest.get(0), rest.get(1), rest.get(2),
                rest.get(3), rest.get(4), untouched));

        castBattalion();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(rest);
    }

    private void castBattalion() {
        harness.setHand(player1, List.of(new ThreeTreeBattalion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
