package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhirlwindKillerCyclone.class, GrizzlyBears.class, FountainOfYouth.class})
class WhirlwindKillerCycloneTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that entered this turn attacking a player makes that player's creature unable to block")
    void enteredCreatureAttackingTriggers() {
        harness.addToBattlefield(player1, new WhirlwindKillerCyclone());
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The trigger does not fire when no attacking creature entered this turn")
    void existingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new WhirlwindKillerCyclone());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger can target only a creature controlled by the attacked player")
    void cannotTargetOwnCreatureOrNoncreature() {
        harness.addToBattlefield(player1, new WhirlwindKillerCyclone());
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Whirlwind can trigger its own ability on the turn it enters")
    void whirlwindEnteringAndAttackingTriggers() {
        harness.enterBattlefieldAndReturn(player1, new WhirlwindKillerCyclone());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Multiple creatures entering this turn and attacking the same player produce one trigger")
    void multipleQualifyingAttackersTriggerOnce() {
        harness.addToBattlefield(player1, new WhirlwindKillerCyclone());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setSummoningSick(false);
        second.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        harness.handlePermanentChosen(player1, blocker.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        assertThat(otherBlocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The trigger still resolves after the qualifying creature stops attacking")
    void attackerLeavingCombatDoesNotPreventResolution() {
        harness.addToBattlefield(player1, new WhirlwindKillerCyclone());
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, blocker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }
}
