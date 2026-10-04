package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.p.PlanTheHeist;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FblthpLostOnTheRange.class, GrizzlyBears.class, Plains.class,
        PlanTheHeist.class, FinalShowdown.class, FailedFording.class, AncestralVision.class})
class FblthpLostOnTheRangeTest extends BaseCardTest {

    @Test
    void plotsNonlandCardFromLibraryTopForItsManaCost() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .contains(bears.getId());
        assertThat(gd.plottedCardIds).contains(bears.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotPlotLandFromLibraryTop() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(plains);
        assertThat(gd.plottedCardIds).doesNotContain(plains.getId());
    }

    @Test
    void canUsePrintedPlotCostFromLibraryTop() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        PlanTheHeist heist = new PlanTheHeist();
        harness.setLibrary(player1, List.of(heist));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        assertThat(gd.plottedCardIds).contains(heist.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotPlotAfterFblthpLosesItsAbilities() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        PlanTheHeist heist = new PlanTheHeist();
        harness.setLibrary(player1, List.of(heist));
        harness.setHand(player1, List.of(new FinalShowdown()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0}, List.of());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(heist);
        assertThat(gd.plottedCardIds).doesNotContain(heist.getId());
    }

    @Test
    void cannotPlotCardWithNoManaCost() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        AncestralVision vision = new AncestralVision();
        harness.setLibrary(player1, List.of(vision));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(vision);
        assertThat(gd.plottedCardIds).doesNotContain(vision.getId());
    }

    @Test
    void fblthpDoesNotRevealItselfWhileInLibrary() {
        harness.setLibrary(player1, List.of(new FblthpLostOnTheRange()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{"));
    }

    @Test
    void topCardIsVisibleOnlyToControllerWithoutPriority() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        harness.setLibrary(player1, List.of(new PlanTheHeist()));
        harness.forceActivePlayer(player2);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{"));
    }

    @Test
    void cannotPlotDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        PlanTheHeist heist = new PlanTheHeist();
        harness.setLibrary(player1, List.of(heist));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(heist);
    }

    @Test
    void plottedCardCannotBeCastUntilLaterTurn() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        FblthpLostOnTheRange plotted = new FblthpLostOnTheRange();
        harness.setLibrary(player1, List.of(plotted));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromExile(player1, plotted.getId()))
                .isInstanceOf(IllegalStateException.class);
        gd.turnNumber++;
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.castFromExile(player1, plotted.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fblthp, Lost on the Range");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPay() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        harness.setHand(player2, List.of(new FailedFording()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Fblthp, Lost on the Range"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fblthp, Lost on the Range");
        harness.assertInGraveyard(player2, "Failed Fording");
    }

    @Test
    void payingWardAllowsOpponentsSpellToResolve() {
        harness.addToBattlefield(player1, new FblthpLostOnTheRange());
        harness.setHand(player2, List.of(new FailedFording()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Fblthp, Lost on the Range"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fblthp, Lost on the Range");
        harness.assertInHand(player1, "Fblthp, Lost on the Range");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
