package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.cards.c.ChillingShade;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RimeboundDead;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhobianPhantasm.class, BorealGriffin.class, ChillingShade.class, Ornithopter.class, RimeboundDead.class})
class PhobianPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Phobian Phantasm")
    void paysCumulativeUpkeep() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new PhobianPhantasm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(phantasm.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phantasm);
    }

    @Test
    @DisplayName("Cumulative upkeep costs one black mana per age counter")
    void cumulativeUpkeepScalesWithAgeCounters() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new PhobianPhantasm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(phantasm.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phantasm);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Phobian Phantasm")
    void declineSacrifices() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new PhobianPhantasm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(phantasm);
        harness.assertInGraveyard(player1, "Phobian Phantasm");
    }

    @Test
    @DisplayName("Fear prevents non-black creatures from blocking Phobian Phantasm")
    void fearPreventsNonBlackBlockers() {
        addCreatureReady(player1, new PhobianPhantasm());

        addCreatureReady(player2, new BorealGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Fear allows black creatures to block Phobian Phantasm")
    void fearAllowsBlackBlockers() {
        addCreatureReady(player1, new PhobianPhantasm());

        addCreatureReady(player2, new ChillingShade());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Fear allows artifact creatures to block Phobian Phantasm")
    void fearAllowsArtifactBlockers() {
        addCreatureReady(player1, new PhobianPhantasm());

        addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Cumulative upkeep does not trigger on the opponent's upkeep")
    void opponentUpkeepDoesNotAddAgeCounter() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new PhobianPhantasm());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(phantasm.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phantasm);
    }

    @Test
    @DisplayName("Cumulative upkeep includes age counters already on the permanent")
    void upkeepCountsExistingAgeCounters() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new PhobianPhantasm());
        phantasm.setCounterCount(CounterType.AGE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(phantasm.getCounterCount(CounterType.AGE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phantasm);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Insufficient mana cannot partially pay cumulative upkeep")
    void insufficientManaCannotPartiallyPayUpkeep() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new PhobianPhantasm());
        phantasm.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(phantasm);
        harness.assertInGraveyard(player1, "Phobian Phantasm");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying still prevents a black creature without flying or reach from blocking")
    void flyingPreventsBlackGroundBlocker() {
        addCreatureReady(player1, new PhobianPhantasm());
        addCreatureReady(player2, new RimeboundDead());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }
}
