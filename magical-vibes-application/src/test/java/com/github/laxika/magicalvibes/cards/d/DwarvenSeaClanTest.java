package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HeartWolf;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeapingLizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenSeaClan.class, DwarvenTrader.class, HeartWolf.class, Island.class, LeapingLizard.class})
class DwarvenSeaClanTest extends BaseCardTest {

    @Test
    @DisplayName("Damage is dealt as end of combat begins, killing a 2/2 attacker")
    void dealsTwoDamageAtEndOfCombat() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new HeartWolf());
        harness.addToBattlefield(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);

        harness.activateAbility(player1, indexOf(player1, clan), 0, null, attacker.getId());
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertInGraveyard(player2, "Heart Wolf");
    }

    @Test
    @DisplayName("A creature that survives the 2 damage stays on the battlefield with damage marked")
    void survivorKeepsMarkedDamage() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new LeapingLizard());
        harness.addToBattlefield(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);

        harness.activateAbility(player1, indexOf(player1, clan), 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leaping Lizard");
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not deal damage if the target stops attacking before resolution")
    void targetMustStillBeAttackingOnResolution() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new DwarvenTrader());
        harness.addToBattlefield(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);

        harness.activateAbility(player1, indexOf(player1, clan), 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dwarven Trader");
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target an attacking creature whose controller controls no Island")
    void cannotTargetWithoutIsland() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new DwarvenTrader());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);

        int index = indexOf(player1, clan);
        UUID targetId = attacker.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatant() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent bystander = addCreatureReady(player2, new DwarvenTrader());
        harness.addToBattlefield(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        int index = indexOf(player1, clan);
        UUID targetId = bystander.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a blocking creature whose controller controls an Island")
    void canTargetBlockingCreature() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new DwarvenTrader());
        Permanent blocker = addCreatureReady(player1, new HeartWolf());
        harness.addToBattlefield(player1, new Island());

        declareAttackers(player2, List.of(indexOf(player2, attacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, blocker), indexOf(player2, attacker))));

        harness.activateAbility(player1, indexOf(player1, clan), 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Heart Wolf");

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.assertInGraveyard(player1, "Heart Wolf");
    }

    @Test
    @DisplayName("Cannot be activated during the end of combat step")
    void cannotActivateDuringEndOfCombat() {
        Permanent clan = addCreatureReady(player1, new DwarvenSeaClan());
        Permanent attacker = addCreatureReady(player2, new DwarvenTrader());
        harness.addToBattlefield(player2, new Island());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        attacker.setAttacking(true);

        int index = indexOf(player1, clan);
        UUID targetId = attacker.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before the end of combat step");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
