package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CaveTiger;
import com.github.laxika.magicalvibes.cards.f.FaultLine;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.z.Zephid;
import com.github.laxika.magicalvibes.cards.w.Wirecat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lifeline.class, CaveTiger.class, Wirecat.class, HeatRay.class,
        FaultLine.class, Zephid.class})
class LifelineTest extends BaseCardTest {

    private void heatRayCaveTigerWithSurvivingWirecat() {
        harness.addToBattlefield(player1, new Lifeline());
        harness.addToBattlefield(player2, new Wirecat());
        harness.addToBattlefield(player2, new CaveTiger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID caveTigerId = harness.getPermanentId(player2, "Cave Tiger");
        harness.castInstant(player1, 0, 2, caveTigerId);
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }

    @Test
    @DisplayName("A dead creature returns to its owner's control at the next end step")
    void returnsDeadCreatureUnderOwnersControl() {
        heatRayCaveTigerWithSurvivingWirecat();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);
        harness.assertInGraveyard(player2, "Cave Tiger");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Cave Tiger");
        harness.assertNotOnBattlefield(player1, "Cave Tiger");
        harness.assertNotInGraveyard(player2, "Cave Tiger");
    }

    @Test
    @DisplayName("Lifeline does not trigger when no other creature is on the battlefield")
    void doesNotTriggerWithoutAnotherCreature() {
        harness.addToBattlefield(player1, new Lifeline());
        harness.addToBattlefield(player2, new CaveTiger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Cave Tiger"));
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
        advanceToEndStep();
        harness.assertInGraveyard(player2, "Cave Tiger");
    }

    @Test
    @DisplayName("The other-creature condition is checked again when Lifeline's trigger resolves")
    void checksAnotherCreatureAgainOnResolution() {
        harness.addToBattlefield(player1, new Lifeline());
        harness.addToBattlefield(player2, new Wirecat());
        harness.addToBattlefield(player2, new CaveTiger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HeatRay(), new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Cave Tiger"));
        harness.passBothPriorities();

        harness.castInstant(player1, 0, 4, harness.getPermanentId(player2, "Wirecat"));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
        harness.assertInGraveyard(player2, "Cave Tiger");
    }

    @Test
    @DisplayName("Each creature dying together returns when another creature survives")
    void returnsEachCreatureFromSimultaneousDeath() {
        harness.addToBattlefield(player1, new Lifeline());
        harness.addToBattlefield(player2, new CaveTiger());
        harness.addToBattlefield(player2, new Wirecat());
        harness.addToBattlefield(player2, new Zephid());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FaultLine()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(2);
        harness.assertInGraveyard(player2, "Cave Tiger");
        harness.assertInGraveyard(player2, "Wirecat");
        harness.assertOnBattlefield(player2, "Zephid");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Cave Tiger");
        harness.assertOnBattlefield(player2, "Wirecat");
        harness.assertNotInGraveyard(player2, "Cave Tiger");
        harness.assertNotInGraveyard(player2, "Wirecat");
    }
}
