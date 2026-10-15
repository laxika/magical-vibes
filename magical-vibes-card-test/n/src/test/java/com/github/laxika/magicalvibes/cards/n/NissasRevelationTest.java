package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.r.Revenant;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissasRevelation.class, Forest.class, DwynensElite.class, Revenant.class, YokedOx.class})
class NissasRevelationTest extends BaseCardTest {

    private void castNissasRevelation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NissasRevelation()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);
    }

    @Test
    void zeroPowerCreatureGainsLifeWithoutDrawing() {
        Card ox = new YokedOx();
        harness.setLibrary(player1, List.of(ox, new Forest(), new Forest(), new Forest(), new Forest()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        keepAllOnTop();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(ox);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
    }

    @Test
    void emptyLibraryResolvesWithoutDrawingOrGainingLife() {
        harness.setLibrary(player1, List.of());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Nissa's Revelation");
    }

    @Test
    void shortLibraryScriesOnlyAvailableCards() {
        Card creature = new DwynensElite();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, creature));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(forest, creature);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    void bottomingAllFiveRevealsPreviouslySixthCard() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card fifth = new Forest();
        Card creature = new DwynensElite();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, creature));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2, 3, 4)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, fourth, fifth);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    void revealedCreatureUsesCharacteristicDefiningAbilityInLibrary() {
        Card revenant = new Revenant();
        Card second = new Forest();
        harness.setGraveyard(player1, List.of(new DwynensElite(), new YokedOx()));
        harness.setGraveyard(player2, List.of(new DwynensElite()));
        harness.setLibrary(player1, List.of(revenant, second, new Forest(), new Forest(), new Forest()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        keepAllOnTop();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revenant, second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    private void keepAllOnTop() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2, 3, 4), List.of()));
    }

    @Test
    @DisplayName("Resolving enters a scry 5 interaction")
    void entersScryFive() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new DwynensElite()));

        castNissasRevelation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("Revealed creature draws cards equal to its power and gains life equal to its toughness")
    void revealedCreatureDrawsAndGainsLife() {
        Card top = new DwynensElite();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(top, second, new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        keepAllOnTop();

        // 2/2: draws 2 (the revealed card itself first, since it stays on top) and gains 2 life.
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Revealed non-creature stays on top with no draw and no life gain")
    void revealedNonCreatureDoesNothing() {
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top, new Forest(), new Forest(), new Forest(),
                new Forest(), new DwynensElite()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        keepAllOnTop();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Scry reorder decides which card is revealed")
    void scryReorderDecidesRevealedCard() {
        Card forest = new Forest();
        Card bears = new DwynensElite();
        harness.setLibrary(player1, List.of(forest, bears, new Forest(), new Forest(),
                new Forest(), new Forest()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castNissasRevelation();
        // Put the Forest on the bottom so the Dwynen's Elite is revealed instead.
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 2, 3, 4), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }
}
