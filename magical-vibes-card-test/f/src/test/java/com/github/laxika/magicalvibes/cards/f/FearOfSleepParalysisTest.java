package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NestingGrounds;
import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfSleepParalysis.class, DazzlingTheaterPropRoom.class, GloriousAnthem.class, GrizzlyBears.class,
        VampireHexmage.class, NestingGrounds.class})
class FearOfSleepParalysisTest extends BaseCardTest {

    @Test
    void tapsAndStunsUpToOneCreatureWhenAnEnchantmentEnters() {
        harness.addToBattlefield(player1, new FearOfSleepParalysis());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void tapsAndStunsUpToOneCreatureWhenARoomIsFullyUnlocked() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        Permanent room = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.addToBattlefield(player1, new FearOfSleepParalysis());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void tapsAndStunsAChosenCreatureWhenItEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FearOfSleepParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void enteringCreatureCanAddAStunCounterToAnAlreadyTappedFriendlyCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        creature.setCounterCount(CounterType.STUN, 1);
        harness.setHand(player1, List.of(new FearOfSleepParalysis()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void opponentsStunCountersRemainThroughRepeatedUntapSteps() {
        harness.addToBattlefield(player1, new FearOfSleepParalysis());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FearOfSleepParalysis());
        creature.tap();
        creature.setCounterCount(CounterType.STUN, 1);

        harness.performUntapStep(player2);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void controllersStunCountersAreRemovedNormally() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FearOfSleepParalysis());
        creature.tap();
        creature.setCounterCount(CounterType.STUN, 1);

        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("\"Remove all counters\" leaves the stun counters of a permanent an opponent controls")
    void removeAllCountersLeavesOpponentsStunCounters() {
        harness.addToBattlefield(player1, new FearOfSleepParalysis());
        addCreatureReady(player2, new VampireHexmage());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.STUN, 1);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A stun counter on a permanent an opponent controls can't be moved (Fear of Sleep Paralysis ruling)")
    void opponentsStunCounterCantBeMoved() {
        harness.addToBattlefield(player1, new FearOfSleepParalysis());
        addCreatureReady(player2, new NestingGrounds());
        Permanent stunned = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        stunned.setCounterCount(CounterType.STUN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player2, 0, 1, List.of(stunned.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(stunned.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.STUN)).isZero();
    }
}
