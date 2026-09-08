package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TormentorsHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent helm = addReady(player1, new TormentorsHelm());
        helm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {1} attaches to target creature")
    void equipAttaches() {
        Permanent helm = addReady(player1, new TormentorsHelm());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Becoming blocked makes the defending player take 1 damage")
    void blockedCreatureDamagesDefendingPlayer() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent helm = addReady(player1, new TormentorsHelm());
        helm.setAttachedTo(creature.getId());
        Permanent blocker = addReady(player2, new GrizzlyBears());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        declareBlock(creature, blocker);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger fires only once when multiple creatures block")
    void multipleBlockersTriggerOnce() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent helm = addReady(player1, new TormentorsHelm());
        helm.setAttachedTo(creature.getId());
        Permanent firstBlocker = addReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addReady(player2, new GrizzlyBears());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(creature)),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(creature))));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An unattached Helm does not trigger")
    void unattachedHelmDoesNotTrigger() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        addReady(player1, new TormentorsHelm());
        Permanent blocker = addReady(player2, new GrizzlyBears());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        declareBlock(creature, blocker);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private void setLifeTotals(int player1Life, int player2Life) {
        harness.setLife(player1, player1Life);
        harness.setLife(player2, player2Life);
    }
}
