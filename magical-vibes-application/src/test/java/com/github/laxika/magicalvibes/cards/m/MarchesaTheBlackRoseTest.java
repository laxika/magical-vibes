package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchesaTheBlackRose.class, GrizzlyBears.class, LightningBolt.class})
class MarchesaTheBlackRoseTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have dethrone")
    void grantsDethroneToOtherCreaturesYouControl() {
        addMarchesa();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DETHRONE)).isTrue();
    }

    @Test
    @DisplayName("A countered creature you control returns at the next end step")
    void returnsCounteredCreatureAtNextEndStep() {
        addMarchesa();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyBears();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature without a +1/+1 counter does not return")
    void doesNotReturnUncounteredCreature() {
        addMarchesa();
        addCreatureReady(player1, new GrizzlyBears());

        destroyBears();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Marchesa returns herself if she dies with a +1/+1 counter")
    void returnsItselfWithCounter() {
        Permanent marchesa = addCreatureReady(player1, new MarchesaTheBlackRose());
        marchesa.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        marchesa.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Marchesa, the Black Rose");
        harness.assertNotInGraveyard(player1, "Marchesa, the Black Rose");
    }

    private void addMarchesa() {
        addCreatureReady(player1, new MarchesaTheBlackRose());
    }

    private void destroyBears() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }
}
