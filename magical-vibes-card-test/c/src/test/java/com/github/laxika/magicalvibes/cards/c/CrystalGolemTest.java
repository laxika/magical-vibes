package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrystalGolem.class, Forest.class})
class CrystalGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Crystal Golem phases out at the beginning of its controller's end step")
    void phasesOutAtControllerEndStep() {
        Permanent golem = addGolem();

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(golem);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(golem);
    }

    @Test
    @DisplayName("Only the Golem phases out — the trigger phases out no other permanent")
    void phasesOutOnlyItself() {
        Permanent golem = addGolem();
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve the Golem's trigger only

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(golem);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bystander);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentPermanent);
    }

    @Test
    @DisplayName("Crystal Golem does not phase out during the opponent's end step")
    void doesNotPhaseOutAtOpponentEndStep() {
        Permanent golem = addGolem();

        advanceToEndStep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
    }

    @Test
    @DisplayName("Crystal Golem phases back in during its controller's next untap step")
    void phasesBackInNextUntapStep() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent golem = addGolem();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(golem);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(golem);
    }

    private Permanent addGolem() {
        return addCreatureReady(player1, new CrystalGolem());
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Phasing out waits for the end-step trigger to resolve")
    void remainsOnBattlefieldUntilTriggerResolves() {
        Permanent golem = addGolem();

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(golem);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(golem);
    }

    @Test
    @DisplayName("Crystal Golem entering after the end step begins waits until the next end step")
    void enteringDuringEndStepDoesNotTriggerImmediately() {
        advanceToEndStep(player1);
        Permanent golem = harness.enterBattlefieldAndReturn(player1, new CrystalGolem());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
    }

    @Test
    @DisplayName("Crystal Golem stays phased out through the opponent's untap step")
    void staysPhasedOutDuringOpponentUntap() {
        Permanent golem = addGolem();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.performUntapStep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(golem);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(golem);
    }

    @Test
    @DisplayName("Crystal Golem keeps its counters and untaps after phasing in")
    void retainsCountersAndUntapsWhenPhasingIn() {
        Permanent golem = addGolem();
        golem.tap();
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(golem);
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(golem.isTapped()).isFalse();
    }
}
