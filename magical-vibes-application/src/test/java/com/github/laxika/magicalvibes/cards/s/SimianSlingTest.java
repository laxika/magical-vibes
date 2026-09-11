package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimianSling.class, GrizzlyBears.class})
class SimianSlingTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent sling = addReady(player1, new SimianSling());
        sling.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Reconfigure {2} attaches and unattaches the Sling")
    void reconfigureAttachesAndUnattaches() {
        Permanent sling = addReady(player1, new SimianSling());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sling.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, sling)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(sling.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, sling)).isTrue();
    }

    @Test
    @DisplayName("An equipped creature becoming blocked deals 1 damage to the defending player")
    void equippedCreatureBecomingBlockedDamagesDefendingPlayer() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent sling = addReady(player1, new SimianSling());
        sling.setAttachedTo(creature.getId());
        Permanent blocker = addReady(player2, new GrizzlyBears());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        declareBlock(creature, blocker);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The Sling itself deals 1 damage when it becomes blocked")
    void slingBecomingBlockedDamagesDefendingPlayer() {
        Permanent sling = addReady(player1, new SimianSling());
        Permanent blocker = addReady(player2, new GrizzlyBears());
        sling.setAttacking(true);
        sling.setAttackTarget(player2.getId());
        setLifeTotals(20, 20);

        declareBlock(sling, blocker);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger fires only once when multiple creatures block")
    void multipleBlockersTriggerOnce() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent sling = addReady(player1, new SimianSling());
        sling.setAttachedTo(creature.getId());
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
    @DisplayName("Reconfigure cannot target an opponent's creature")
    void reconfigureCannotTargetOpponentsCreature() {
        Permanent sling = addReady(player1, new SimianSling());
        Permanent opponentCreature = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sling.getAttachedTo()).isNull();
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
