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

    private Permanent addReadySource() {
        return addCreatureReady(player1, new KrangShredder());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
