package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleshCarver.class, GrizzlyBears.class, WrathOfGod.class, BurnishedHart.class})
class FleshCarverTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts two +1/+1 counters on Flesh Carver")
    void sacrificingAnotherCreaturePutsTwoCountersOnFleshCarver() {
        Permanent fleshCarver = harness.addToBattlefieldAndReturn(player1, new FleshCarver());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fleshCarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, fleshCarver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fleshCarver)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Flesh Carver cannot sacrifice itself")
    void cannotSacrificeItself() {
        harness.addToBattlefield(player1, new FleshCarver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When Flesh Carver dies, it creates a black Horror with power and toughness equal to its power")
    void deathCreatesHorrorEqualToDyingPower() {
        harness.addToBattlefield(player1, new FleshCarver());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent horror = findPermanent(player1, "Horror");
        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Flesh Carver");
    }

    @Test
    void deathCreatesTokenWithoutCounters() {
        harness.addToBattlefield(player1, new FleshCarver());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent horror = findPermanent(player1, "Horror");
        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(2);
        assertThat(horror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sacrificeIsPaidBeforeCountersAreAdded() {
        Permanent fleshCarver = harness.addToBattlefieldAndReturn(player1, new FleshCarver());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        assertThat(fleshCarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(fleshCarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new FleshCarver());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void intimidatePreventsGreenNonartifactCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new FleshCarver());
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void intimidateAllowsBlackCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new FleshCarver());
        Permanent blocker = addCreatureReady(player2, new FleshCarver());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void intimidateAllowsColorlessArtifactCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new FleshCarver());
        Permanent blocker = addCreatureReady(player2, new BurnishedHart());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
