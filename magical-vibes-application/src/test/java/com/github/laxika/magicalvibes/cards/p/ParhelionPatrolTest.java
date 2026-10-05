package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HuntedWitness;
import com.github.laxika.magicalvibes.cards.o.OrneryGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParhelionPatrol.class, HuntedWitness.class, OrneryGoblin.class})
class ParhelionPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new ParhelionPatrol());
        Permanent attackingWitness = addCreatureReady(player1, new HuntedWitness());
        Permanent nonAttackingWitness = addCreatureReady(player1, new HuntedWitness());
        Permanent equalPowerCreature = addCreatureReady(player1, new OrneryGoblin());

        declareAttackers(List.of(0, 1, 3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingWitness.getId());

        harness.handlePermanentChosen(player1, attackingWitness.getId());
        resolveAllTriggers();

        assertThat(attackingWitness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingWitness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor uses the source's last known power if it leaves before resolution")
    void mentorUsesSourceLastKnownPower() {
        Permanent patrol = addCreatureReady(player1, new ParhelionPatrol());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        gd.playerBattlefields.get(player1.getId()).remove(patrol);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mentorDoesNotAddCounterIfTargetReachesEqualPower() {
        addCreatureReady(player1, new ParhelionPatrol());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        witness.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mentorRechecksSourcePowerAtResolution() {
        Permanent patrol = addCreatureReady(player1, new ParhelionPatrol());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        patrol.setPowerModifier(-1);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mentorUsesBoostedPowerWhenChoosingTarget() {
        Permanent patrol = addCreatureReady(player1, new ParhelionPatrol());
        Permanent goblin = addCreatureReady(player1, new OrneryGoblin());
        patrol.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(goblin.getId());
        harness.handlePermanentChosen(player1, goblin.getId());
        resolveAllTriggers();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void twoMentorsOnlyAddOneCounterWhenFirstMakesTargetEqualPower() {
        addCreatureReady(player1, new ParhelionPatrol());
        addCreatureReady(player1, new ParhelionPatrol());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1, 2));
        harness.handlePermanentChosen(player1, witness.getId());
        harness.handlePermanentChosen(player1, witness.getId());
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mentorDoesNotAddCounterToCreatureRemovedFromCombat() {
        addCreatureReady(player1, new ParhelionPatrol());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        witness.setAttacking(false);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mentorUsesPowerAtDepartureRatherThanAtTriggerTime() {
        Permanent patrol = addCreatureReady(player1, new ParhelionPatrol());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        patrol.setPowerModifier(2);
        witness.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, patrol));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Parhelion Patrol");
        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void mentorUsesReducedPowerAtDeparture() {
        Permanent patrol = addCreatureReady(player1, new ParhelionPatrol());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        patrol.setPowerModifier(-1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, patrol));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Parhelion Patrol");
        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attacksAloneWithoutMentorTargetAndRemainsUntapped() {
        Permanent patrol = addCreatureReady(player1, new ParhelionPatrol());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(patrol.isTapped()).isFalse();
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new ParhelionPatrol());
        addCreatureReady(player2, new HuntedWitness());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
