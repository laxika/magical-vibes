package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterOfCrueltiesTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking unblocked sets the defending player's life to 1 and prevents its combat damage")
    void unblockedSetsLifeToOneAndAssignsNoDamage() {
        Permanent master = addAttackingMaster(player1, player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(master.getId());
    }

    @Test
    @DisplayName("Being blocked leaves the defending player's life alone")
    void blockedDoesNotSetLife() {
        Permanent master = addAttackingMaster(player1, player2);
        addCreatureReady(player2, new GrizzlyBears());
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(master.getId());
    }

    @Test
    @DisplayName("Can be declared as the sole attacker")
    void attacksAlone() {
        addCreatureReady(player1, new MasterOfCruelties());
        addCreatureReady(player1, new GrizzlyBears());

        beginAttackerDeclaration();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can't be declared as an attacker alongside another creature")
    void cannotAttackWithOthers() {
        addCreatureReady(player1, new MasterOfCruelties());
        addCreatureReady(player1, new GrizzlyBears());

        beginAttackerDeclaration();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only attack alone");
    }

    private void beginAttackerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addAttackingMaster(Player attacker, Player defender) {
        Permanent perm = new Permanent(new MasterOfCruelties());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attacker.getId()).add(perm);
        return perm;
    }
}
