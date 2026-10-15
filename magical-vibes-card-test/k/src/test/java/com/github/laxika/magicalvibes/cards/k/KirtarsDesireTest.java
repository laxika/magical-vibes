package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({KirtarsDesire.class, DuskImp.class, Plains.class})
class KirtarsDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new DuskImp());
        attachAura(creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(indexOf(player1, creature))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature can block before threshold")
    void enchantedCreatureCanBlockBeforeThreshold() {
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        attachAura(blocker);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot block at threshold")
    void enchantedCreatureCannotBlockAtThreshold() {
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()));
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        attachAura(blocker);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Opponent graveyard does not enable the threshold ability")
    void opponentGraveyardDoesNotEnableThresholdAbility() {
        harness.setGraveyard(player2, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(),
                new DuskImp(), new DuskImp(), new DuskImp()));
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        attachAura(blocker);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Kirtar's Desire can target only a creature")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new KirtarsDesire()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving the Aura attaches it and prevents the targeted creature attacking")
    void resolvesOntoCreature() {
        Permanent creature = addCreatureReady(player1, new DuskImp());
        harness.setHand(player1, List.of(new KirtarsDesire()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Kirtar's Desire").getAttachedTo()).isEqualTo(creature.getId());
        assertThatThrownBy(() -> declareAttackers(player1, List.of(indexOf(player1, creature))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Reaching threshold after attachment prevents blocking")
    void gainingThresholdPreventsBlocking() {
        harness.setGraveyard(player1, List.of(
                new Plains(), new Plains(), new Plains(), new Plains(), new Plains(), new Plains()));
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        attachAura(blocker);
        harness.setGraveyard(player1, List.of(
                new Plains(), new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains()));

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Dropping below threshold after attachment allows blocking")
    void losingThresholdAllowsBlocking() {
        harness.setGraveyard(player1, List.of(
                new Plains(), new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains()));
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        attachAura(blocker);
        harness.setGraveyard(player1, List.of(
                new Plains(), new Plains(), new Plains(), new Plains(), new Plains(), new Plains()));

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void attachAura(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KirtarsDesire());
        aura.setAttachedTo(host.getId());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
