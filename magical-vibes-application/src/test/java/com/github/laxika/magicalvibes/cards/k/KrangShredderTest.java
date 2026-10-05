package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrangShredder.class, GrizzlyBears.class, Island.class})
class KrangShredderTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each opponent exiles cards until a nonland card")
    void enteringExilesUntilNonland() {
        Island land = new Island();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland));

        Permanent source = harness.enterBattlefieldAndReturn(player1, new KrangShredder());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .extracting(ExiledCardEntry::card)
                .containsExactly(land, nonland);
        assertThat(gd.exiledCards)
                .allMatch(entry -> source.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("Attacking repeats the until-nonland exile and tracks the cards with the source")
    void attackingExilesUntilNonland() {
        Island land = new Island();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland));
        Permanent source = addReadySource();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.exiledCards)
                .extracting(ExiledCardEntry::card)
                .containsExactly(land, nonland);
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(land, nonland);
    }

    @Test
    @DisplayName("At the end step, a permanent leaving this turn enables one free cast")
    void endStepMayCastExiledCardAfterPermanentLeaves() {
        GrizzlyBears exiledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledCard));
        Permanent source = harness.enterBattlefieldAndReturn(player1, new KrangShredder());
        harness.passBothPriorities();

        Permanent leavingPermanent = addCreatureReady(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, leavingPermanent));

        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
    }

    @Test
    void enteringTriggerStillExilesAfterSourceLeaves() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(nonland));
        Permanent source = harness.enterBattlefieldAndReturn(player1, new KrangShredder());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(nonland);
    }

    @Test
    void allLandLibraryIsExiledWithoutOfferingALandCast() {
        Island first = new Island();
        Island second = new Island();
        harness.setLibrary(player2, List.of(first, second));
        Permanent source = harness.enterBattlefieldAndReturn(player1, new KrangShredder());
        harness.passBothPriorities();
        Permanent leaving = addCreatureReady(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, leaving));

        advanceToEndStep();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Island");
    }

    @Test
    void disappearDoesNotTriggerWithoutAControlledPermanentLeaving() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(nonland));
        harness.enterBattlefieldAndReturn(player1, new KrangShredder());
        harness.passBothPriorities();
        Permanent opposingPermanent = addCreatureReady(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, opposingPermanent));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
    }

    @Test
    void decliningFreeCastLeavesCardExiled() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(nonland));
        harness.enterBattlefieldAndReturn(player1, new KrangShredder());
        harness.passBothPriorities();
        Permanent leaving = addCreatureReady(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, leaving));
        advanceToEndStep();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void oneDisappearTriggerCastsOnlyOneOfMultipleExiledSpells() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second));
        Permanent source = harness.enterBattlefieldAndReturn(player1, new KrangShredder());
        harness.passBothPriorities();
        source.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        Permanent leaving = addCreatureReady(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, leaving));
        advanceToEndStep();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadySource() {
        return addCreatureReady(player1, new KrangShredder());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
