package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagmaticCore.class, BorealCentaur.class})
class MagmaticCoreTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, distributes damage equal to its age counters among target creatures")
    void distributesDamageAmongTargetCreatures() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());
        core.setCounterCount(CounterType.AGE, 3);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, firstCreature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        PendingInteraction.XValueChoice allocation =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(allocation).isNotNull();
        assertThat(allocation.minValue()).isEqualTo(1);
        assertThat(allocation.maxValue()).isEqualTo(2);
        harness.handleXValueChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondCreature.getCard());
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerAtOpponentEndStep() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());
        core.setCounterCount(CounterType.AGE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep adds an age counter and can be paid")
    void cumulativeUpkeepAddsAgeCounterAndCanBePaid() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(core.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(core);
    }

    @Test
    @DisplayName("Cumulative upkeep costs one mana for each age counter")
    void cumulativeUpkeepCostScalesWithAgeCounters() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());
        core.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(core.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(core);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Magmatic Core")
    void decliningCumulativeUpkeepSacrificesCore() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(core.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Magmatic Core");
        harness.assertInGraveyard(player1, "Magmatic Core");
    }

    @Test
    @DisplayName("End-step damage can target creatures but not noncreature permanents")
    void endStepTargetsOnlyCreatures() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());
        core.setCounterCount(CounterType.AGE, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new MagmaticCore());

        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(creature.getId()).doesNotContain(noncreature.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("End-step damage may choose no target creatures")
    void endStepMayChooseNoTargets() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());
        core.setCounterCount(CounterType.AGE, 3);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(core);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.CLEANUP, harness::passBothPriorities);
    }
}
