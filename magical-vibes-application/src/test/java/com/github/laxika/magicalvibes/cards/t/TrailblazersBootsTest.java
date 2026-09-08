package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrailblazersBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature cannot be blocked while defending player controls a nonbasic land")
    void cannotBeBlockedWithNonbasicLand() {
        harness.addToBattlefield(player2, new TreetopVillage());
        Permanent blocker = addCreature(player2);
        Permanent attacker = addCreature(player1);
        Permanent boots = new Permanent(new TrailblazersBoots());
        gd.playerBattlefields.get(player1.getId()).add(boots);
        boots.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        prepareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Equipped creature can be blocked while defending player controls only basic lands")
    void canBeBlockedWithBasicLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreature(player2);
        Permanent attacker = addCreature(player1);
        Permanent boots = new Permanent(new TrailblazersBoots());
        gd.playerBattlefields.get(player1.getId()).add(boots);
        boots.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        prepareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unattached Boots do not grant nonbasic landwalk")
    void losesLandwalkWhenUnattached() {
        harness.addToBattlefield(player2, new TreetopVillage());
        Permanent blocker = addCreature(player2);
        Permanent attacker = addCreature(player1);
        Permanent boots = new Permanent(new TrailblazersBoots());
        gd.playerBattlefields.get(player1.getId()).add(boots);
        boots.setAttachedTo(attacker.getId());
        boots.setAttachedTo(null);
        attacker.setAttacking(true);

        prepareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
