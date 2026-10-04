package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcehideGolem;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlacialRevelation.class, Forest.class, GrizzlyBears.class, Shock.class,
        SnowCoveredForest.class, IcehideGolem.class})
class GlacialRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts any chosen snow permanents into hand and the rest into the graveyard")
    void choosesSnowPermanentsAndMillsTheRest() {
        Card snowForest = snow(new Forest());
        Card snowBears = snow(new GrizzlyBears());
        Card snowShock = snow(new Shock());
        Card nonsnowForest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(snowForest, snowBears, snowShock, nonsnowForest, bears, shock));

        castGlacialRevelation();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(snowForest.getId(), snowBears.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(snowForest.getId(), snowBears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(snowForest, snowBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(snowShock, nonsnowForest, bears, shock);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May decline all snow permanents")
    void mayDeclineAllSnowPermanents() {
        Card snowForest = snow(new Forest());
        Card snowBears = snow(new GrizzlyBears());
        harness.setLibrary(player1, List.of(snowForest, snowBears));

        castGlacialRevelation();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snowForest, snowBears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unchosen snow permanents are milled and cards below the top six stay in the library")
    void choosesOnlySomeOfTheTopSix() {
        Card chosen = new SnowCoveredForest();
        Card unchosen = new IcehideGolem();
        Card third = new GlacialRevelation();
        Card fourth = new GlacialRevelation();
        Card fifth = new GlacialRevelation();
        Card sixth = new GlacialRevelation();
        Card seventh = new SnowCoveredForest();
        Card eighth = new IcehideGolem();
        harness.setLibrary(player1, List.of(chosen, unchosen, third, fourth, fifth, sixth, seventh, eighth));

        castGlacialRevelation();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), unchosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(unchosen, third, fourth, fifth, sixth)
                .doesNotContain(chosen, seventh, eighth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(seventh, eighth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A short library can supply real snow lands and snow artifact creatures")
    void choosesRealSnowPermanentsFromShortLibrary() {
        Card land = new SnowCoveredForest();
        Card creature = new IcehideGolem();
        harness.setLibrary(player1, List.of(land, creature));

        castGlacialRevelation();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId(), creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(land, creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land, creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("No eligible cards are all milled without requiring a choice")
    void millsAllWhenNoCardsAreEligible() {
        Card first = new GlacialRevelation();
        Card second = new GlacialRevelation();
        harness.setLibrary(player1, List.of(first, second));

        castGlacialRevelation();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a failed draw")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castGlacialRevelation();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castGlacialRevelation() {
        harness.setHand(player1, List.of(new GlacialRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private Card snow(Card card) {
        card.setSupertypes(EnumSet.of(CardSupertype.SNOW));
        return card;
    }
}
