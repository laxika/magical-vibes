package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StarfieldShepherd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoidforgedTitan.class, Forest.class, StarfieldShepherd.class})
class VoidforgedTitanTest extends BaseCardTest {

    @Test
    void drawsAndLosesLifeAfterNonlandPermanentLeaves() {
        addReadyTitan();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StarfieldShepherd());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        Card drawn = new Forest();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(drawn));

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player1, 19);
    }

    @Test
    void drawsAndLosesLifeAfterSpellIsWarped() {
        addReadyTitan();
        StarfieldShepherd shepherd = new StarfieldShepherd();
        harness.setHand(player1, List.of(shepherd));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card drawn = new Forest();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(drawn));

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player1, 19);
    }

    @Test
    void doesNotTriggerWithoutVoidEvent() {
        addReadyTitan();
        Card remaining = new Forest();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(remaining));

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotTriggerAfterOnlyLandLeaves() {
        addReadyTitan();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        Card remaining = new Forest();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(remaining));

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player1, 20);
    }

    @Test
    void countsDepartureBeforeTitanEnters() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StarfieldShepherd());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        addReadyTitan();
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player1, 19);
    }

    @Test
    void multipleDeparturesStillDrawOnlyOneCard() {
        addReadyTitan();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StarfieldShepherd());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new StarfieldShepherd());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });
        Card drawn = new Forest();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.setLife(player1, 20);

        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player1, 19);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        addReadyTitan();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StarfieldShepherd());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(remaining));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player1, 20);
    }

    @Test
    void departureAfterEndStepBeginsDoesNotCreateTrigger() {
        addReadyTitan();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StarfieldShepherd());
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(remaining));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player1, 20);
    }

    @Test
    void triggerResolvesAfterTitanLeavesBattlefield() {
        harness.setHand(player1, List.of());
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new VoidforgedTitan());
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StarfieldShepherd());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, titan));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player1, 19);
    }

    private void addReadyTitan() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VoidforgedTitan());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
