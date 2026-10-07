package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RubblebeltRecluse;
import com.github.laxika.magicalvibes.cards.w.WallOfLostThoughts;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TinStreetDodger.class, RubblebeltRecluse.class, WallOfLostThoughts.class})
class TinStreetDodgerTest extends BaseCardTest {

    @Test
    @DisplayName("The ability prevents non-defender creatures from blocking this turn")
    void nonDefenderCannotBlock() {
        Permanent dodger = addReadyDodger(player1);
        Permanent blocker = addCreatureReady(player2, new RubblebeltRecluse());
        activateDodger(dodger);
        dodger.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dodger);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability allows creatures with defender to block this turn")
    void defenderCanBlock() {
        Permanent dodger = addReadyDodger(player1);
        Permanent blocker = addCreatureReady(player2, new WallOfLostThoughts());
        activateDodger(dodger);
        dodger.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dodger);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent dodger = addReadyDodger(player1);
        Permanent blocker = addCreatureReady(player2, new RubblebeltRecluse());
        activateDodger(dodger);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        dodger.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dodger);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Activating one Dodger does not restrict blockers for another")
    void restrictionAppliesOnlyToSource() {
        Permanent source = addReadyDodger(player1);
        Permanent other = addReadyDodger(player1);
        Permanent blocker = addCreatureReady(player2, new RubblebeltRecluse());
        activateDodger(source);
        other.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(other))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability requires red mana")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent dodger = addReadyDodger(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(dodger), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Haste lets a newly entered Dodger attack")
    void newlyEnteredDodgerCanAttack() {
        Permanent dodger = harness.addToBattlefieldAndReturn(player1, new TinStreetDodger());
        dodger.setSummoningSick(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(dodger.isAttacking()).isTrue();
    }

    private Permanent addReadyDodger(Player player) {
        return addCreatureReady(player, new TinStreetDodger());
    }

    private void activateDodger(Permanent dodger) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(dodger), null, null);
        harness.passBothPriorities();
    }

}
