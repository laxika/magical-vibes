package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMoment.class, Disenchant.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class TheMomentTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger puts a time counter on The Moment")
    void upkeepTriggerAddsTimeCounter() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.DRAW);

        assertThat(moment.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps and phases out a target creature until The Moment leaves")
    void untapsAndPhasesOutTargetCreatureUntilSourceLeaves() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, moment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "The Moment");
    }

    @Test
    @DisplayName("Destroys nonland permanents at or below its time-counter threshold, then sacrifices itself")
    void destroysPermanentsAtOrBelowTimeCounterThreshold() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());
        Permanent oneManaPermanent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent twoManaPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        moment.setCounterCount(CounterType.TIME, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(twoManaPermanent, land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(oneManaPermanent);
        harness.assertInGraveyard(player1, "The Moment");
    }

    @Test
    @DisplayName("The phasing ability cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new TheMoment());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("The Moment does not gain a counter during an opponent's upkeep")
    void opponentUpkeepDoesNotAddTimeCounter() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.DRAW);

        assertThat(moment.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    @DisplayName("Removing The Moment in response still untaps the target without phasing it out")
    void sourceRemovedBeforePhasingAbilityResolves() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, moment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "The Moment");
    }

    @Test
    @DisplayName("A protected creature stays phased out through its controller's untap step")
    void protectedCreatureDoesNotPhaseInDuringUntap() {
        harness.addToBattlefield(player1, new TheMoment());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("A phased-out creature survives the wipe and returns when The Moment is destroyed")
    void phasedOutCreatureSurvivesWipe() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        moment.setCounterCount(CounterType.TIME, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Moment");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(protectedCreature.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playersWhoSacrificedPermanentsThisTurn).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("The wipe uses the source's last known time counters if it is destroyed in response")
    void wipeUsesLastKnownCounters() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        moment.setCounterCount(CounterType.TIME, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        moment.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, moment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Moment");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("Zero time counters spare positive-mana-value permanents and lands")
    void zeroCountersStillSacrificesSource() {
        harness.addToBattlefield(player1, new TheMoment());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Moment");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature, land);
    }

    @Test
    @DisplayName("The wipe cannot be activated during upkeep")
    void wipeRequiresMainPhase() {
        harness.addToBattlefield(player1, new TheMoment());
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The wipe cannot be activated with another ability on the stack")
    void wipeRequiresEmptyStack() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        moment.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
