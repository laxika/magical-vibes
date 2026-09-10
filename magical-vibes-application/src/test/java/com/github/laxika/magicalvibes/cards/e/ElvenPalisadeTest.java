package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({ElvenPalisade.class, Forest.class, RagingGoblin.class})
class ElvenPalisadeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest weakens the targeted attacking creature")
    void sacrificesForestToWeakenAttacker() {
        Permanent palisade = harness.addToBattlefieldAndReturn(player1, new ElvenPalisade());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = addAttacker(player1);
        declareAttackerWithBlocker(player1, attacker);

        harness.activateAbility(player1, 0, null, attacker.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(-3);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
        assertThat(attacker.getEffectivePower()).isEqualTo(-2);
        assertThat(palisade.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Only attacking creatures can be targeted")
    void cannotTargetNonAttackingCreature() {
        harness.addToBattlefield(player1, new ElvenPalisade());
        harness.addToBattlefield(player1, new Forest());
        Permanent bystander = addCreatureReady(player1, new RagingGoblin());
        Permanent attacker = addAttacker(player1);
        declareAttackerWithBlocker(player1, attacker);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }

    @Test
    @DisplayName("A Forest is required for the activation cost")
    void requiresForestToSacrifice() {
        harness.addToBattlefield(player1, new ElvenPalisade());
        Permanent attacker = addAttacker(player1);
        declareAttackerWithBlocker(player1, attacker);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacking creature controlled by an opponent can be targeted")
    void canTargetOpponentsAttacker() {
        harness.addToBattlefield(player1, new ElvenPalisade());
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = addAttacker(player2);
        declareAttackerWithBlocker(player2, attacker);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(-3);
        assertThat(attacker.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The ability does nothing if the target stops attacking before resolution")
    void doesNothingIfTargetStopsAttacking() {
        harness.addToBattlefield(player1, new ElvenPalisade());
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = addAttacker(player1);
        declareAttackerWithBlocker(player1, attacker);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getEffectivePower()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void powerReductionWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElvenPalisade());
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = addAttacker(player1);
        declareAttackerWithBlocker(player1, attacker);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(attacker.getPowerModifier()).isEqualTo(-3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("An attacking noncreature cannot be targeted")
    void cannotTargetAttackingNoncreature() {
        harness.addToBattlefield(player1, new ElvenPalisade());
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new Forest());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker(Player player) {
        return addCreatureReady(player, new RagingGoblin());
    }

    private void declareAttackerWithBlocker(Player attackingPlayer, Permanent attacker) {
        Player defendingPlayer = attackingPlayer == player1 ? player2 : player1;
        Permanent blocker = addCreatureReady(defendingPlayer, new RagingGoblin());
        int attackerIndex = gd.playerBattlefields.get(attackingPlayer.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(defendingPlayer.getId()).indexOf(blocker);

        declareAttackers(attackingPlayer, List.of(attackerIndex));
        gs.declareBlockers(gd, defendingPlayer, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
