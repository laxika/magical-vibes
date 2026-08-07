package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
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

class MantaRayTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.setHand(player1, List.of(new MantaRay()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> state trigger fires
        harness.passBothPriorities(); // resolve state trigger -> sacrificed

        harness.assertNotOnBattlefield(player1, "Manta Ray");
        harness.assertInGraveyard(player1, "Manta Ray");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new MantaRay()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Manta Ray");
    }

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent ray = addRay(player1);

        declareRayAttack(ray);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderHasNoIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent ray = addRay(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ray);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be blocked by a non-blue creature")
    void cannotBeBlockedByNonBlueCreature() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent ray = addRay(player1);
        ray.setAttacking(true);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex(bears), attackerIndex(ray)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue creatures");
    }

    @Test
    @DisplayName("Can be blocked by a blue creature")
    void canBeBlockedByBlueCreature() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent ray = addRay(player1);
        ray.setAttacking(true);

        Permanent wizard = new Permanent(new FugitiveWizard());
        wizard.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wizard);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex(wizard), attackerIndex(ray))));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private int attackerIndex(Permanent attacker) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
    }

    private int blockerIndex(Permanent blocker) {
        return gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
    }

    private Permanent addRay(Player player) {
        Permanent perm = new Permanent(new MantaRay());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareRayAttack(Permanent ray) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ray);
        gs.declareAttackers(gd, player1, List.of(index));
    }
}
