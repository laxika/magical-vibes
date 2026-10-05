package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeonardoSewerSamurai.class, GoblinPiker.class, GrizzlyBears.class, Ornithopter.class, Shock.class})
class LeonardoSewerSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature with power 1 or less from the graveyard with a finality counter")
    void castsCreatureWithLowPowerFromGraveyard() {
        Card creature = new Ornithopter();
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of());
        prepareMainPhaseFor(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, creature.getName());
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("casts a creature with toughness 1 or less from the graveyard with a finality counter")
    void castsCreatureWithLowToughnessFromGraveyard() {
        Card creature = new GoblinPiker();
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhaseFor(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, creature.getName());
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("does not cast a creature with power and toughness greater than 1")
    void rejectsCreatureWithHighPowerAndToughness() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhaseFor(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("does not grant graveyard casting permission during an opponent's turn")
    void permissionIsLimitedToControllerTurn() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setHand(player1, List.of());
        prepareMainPhaseFor(player2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNoncreatureSpellsFromGraveyard() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhaseFor(player1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void doesNotGrantPermissionToOpponent() {
        harness.addToBattlefield(player2, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setHand(player1, List.of());
        prepareMainPhaseFor(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardCastingStillRequiresMana() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new GoblinPiker()));
        harness.setHand(player1, List.of());
        prepareMainPhaseFor(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Goblin Piker");
    }

    @Test
    void graveyardPermissionDoesNotGrantFlash() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void finalityCounterExilesCreatureInsteadOfDying() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new GoblinPiker()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhaseFor(player1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Goblin Piker"));

        harness.assertNotOnBattlefield(player1, "Goblin Piker");
        harness.assertNotInGraveyard(player1, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).contains("Goblin Piker");
    }

    @Test
    void finalityCounterPersistsWhenLeonardoLeavesBeforeResolution() {
        Permanent leonardo = harness.addToBattlefieldAndReturn(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setHand(player1, List.of());
        prepareMainPhaseFor(player1);
        harness.castFromGraveyard(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, leonardo));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ornithopter").getCounterCount(CounterType.FINALITY))
                .isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Leonardo, Sewer Samurai");
    }

    @Test
    void handCastDoesNotReceiveFinalityCounter() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setHand(player1, List.of(new Ornithopter()));
        prepareMainPhaseFor(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ornithopter").getCounterCount(CounterType.FINALITY))
                .isZero();
    }

    @Test
    void canCastMultipleEligibleCreaturesInOneTurn() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new Ornithopter(), new Ornithopter()));
        harness.setHand(player1, List.of());
        prepareMainPhaseFor(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ornithopter")).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.FINALITY))
                        .isEqualTo(1));
    }

    @Test
    void sneakReturnsUnblockedAttackerAndDealsDoubleStrikeDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoSewerSamurai()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent leonardo = findPermanent(player1, "Leonardo, Sewer Samurai");
        assertThat(leonardo.isTapped()).isTrue();
        assertThat(leonardo.isAttacking()).isTrue();
        assertThat(leonardo.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(leonardo.getCounterCount(CounterType.FINALITY)).isZero();

        gd.playerAutoStopSteps.clear();
        resolveCombat();
        harness.assertLife(player2, 14);
    }

    @Test
    void sneakCannotBeUsedOutsideDeclareBlockers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoSewerSamurai()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void sneakCannotReturnBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.setHand(player1, List.of(new LeonardoSewerSamurai()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void normalCostDoesNotAllowCastingDuringDeclareBlockers() {
        harness.setHand(player1, List.of(new LeonardoSewerSamurai()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhaseFor(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
