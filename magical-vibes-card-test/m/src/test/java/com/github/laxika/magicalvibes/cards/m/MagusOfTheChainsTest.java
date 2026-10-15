package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.s.StinkweedImp;
import com.github.laxika.magicalvibes.cards.w.WiltLeafLiege;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheChains.class, Forest.class, GrizzlyBears.class, Humble.class,
        WiltLeafLiege.class, StinkweedImp.class})
class MagusOfTheChainsTest extends BaseCardTest {

    @Test
    void firstDrawOfDrawStepIsNotReplaced() {
        harness.addToBattlefield(player1, new MagusOfTheChains());
        CardFixture fixture = new CardFixture();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(fixture.libraryCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        drawCard(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fixture.libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void extraDrawPromptsForDiscardAndThenDraws() {
        harness.addToBattlefield(player1, new MagusOfTheChains());
        CardFixture fixture = new CardFixture();
        harness.setHand(player1, List.of(fixture.handCard));
        harness.setLibrary(player1, List.of(fixture.libraryCard));

        drawOutsideDrawStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fixture.libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fixture.handCard);
    }

    @Test
    void emptyHandMillsInsteadOfDrawing() {
        harness.addToBattlefield(player1, new MagusOfTheChains());
        CardFixture fixture = new CardFixture();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(fixture.libraryCard));

        drawOutsideDrawStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fixture.libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void drawOutsideDrawStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        drawCard(player);
    }

    @Test
    void secondDrawOfDrawStepIsReplaced() {
        harness.addToBattlefield(player1, new MagusOfTheChains());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);

        drawCard(player1);
        drawCard(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    void twoCopiesRequireTwoDiscardsBeforeDrawing() {
        harness.addToBattlefield(player1, new MagusOfTheChains());
        harness.addToBattlefield(player2, new MagusOfTheChains());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(drawn));

        drawOutsideDrawStep(player1);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void millingStopsRemainingCopiesFromReplacingDraw() {
        harness.addToBattlefield(player1, new MagusOfTheChains());
        harness.addToBattlefield(player1, new MagusOfTheChains());
        harness.addToBattlefield(player2, new MagusOfTheChains());
        GrizzlyBears discarded = new GrizzlyBears();
        Forest milled = new Forest();
        Forest remaining = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(milled, remaining));

        drawOutsideDrawStep(player1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded, milled);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void losingAbilitiesStopsDrawReplacement() {
        var magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheChains());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, magus.getId());
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        drawOutsideDrawStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentControlledReplacementPutsDiscardedLiegeOntoBattlefield() {
        harness.addToBattlefield(player2, new MagusOfTheChains());
        WiltLeafLiege liege = new WiltLeafLiege();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(liege));
        harness.setLibrary(player1, List.of(drawn));

        drawOutsideDrawStep(player1);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Wilt-Leaf Liege");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(liege);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void drawingPlayerCanApplyDredgeBeforeMagusWithEmptyHand() {
        harness.addToBattlefield(player2, new MagusOfTheChains());
        StinkweedImp imp = new StinkweedImp();
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(imp));
        harness.setLibrary(player1, milled);

        drawOutsideDrawStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(imp);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private static final class CardFixture {
        private final GrizzlyBears handCard = new GrizzlyBears();
        private final Forest libraryCard = new Forest();
    }
}
