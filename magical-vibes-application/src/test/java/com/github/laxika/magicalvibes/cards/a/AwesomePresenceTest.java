package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AwesomePresenceTest extends BaseCardTest {

    private Permanent attacking(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setAttacking(true);
        return perm;
    }

    private void enchant(Permanent creature, Player auraController) {
        Permanent aura = new Permanent(new AwesomePresence());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
    }

    private void enterDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private int defenderIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player2.getId()).indexOf(permanent);
    }

    private int attackerIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("Blocking the enchanted creature costs the defending player {3}")
    void blockCostsThree() {
        Permanent enchanted = attacking(player1, new GrizzlyBears());
        enchant(enchanted, player1);
        Permanent blocker = addCreatureReady(player2, new ScatheZombies());
        harness.addMana(player2, ManaColor.BLACK, 3);
        enterDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(defenderIndex(blocker), attackerIndex(enchanted))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The block is rejected when the defending player can't pay {3}")
    void blockRejectedWithoutPayment() {
        Permanent enchanted = attacking(player1, new GrizzlyBears());
        enchant(enchanted, player1);
        Permanent blocker = addCreatureReady(player2, new ScatheZombies());
        harness.addMana(player2, ManaColor.BLACK, 2);
        enterDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(defenderIndex(blocker), attackerIndex(enchanted)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay block cost (3 required)");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The {3} is charged for each creature blocking the enchanted creature")
    void costIsChargedPerBlocker() {
        Permanent enchanted = attacking(player1, new GrizzlyBears());
        enchant(enchanted, player1);
        Permanent first = addCreatureReady(player2, new ScatheZombies());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.BLACK, 5);
        enterDeclareBlockers();

        List<BlockerAssignment> bothBlock = List.of(
                new BlockerAssignment(defenderIndex(first), attackerIndex(enchanted)),
                new BlockerAssignment(defenderIndex(second), attackerIndex(enchanted)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, bothBlock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay block cost (6 required)");

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.beginBlockerDeclarationInput();
        assertThatCode(() -> gs.declareBlockers(gd, player2, bothBlock)).doesNotThrowAnyException();

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Only the enchanted creature is taxed; another attacker is blocked for free")
    void otherAttackersAreFree() {
        Permanent enchanted = attacking(player1, new GrizzlyBears());
        enchant(enchanted, player1);
        Permanent free = attacking(player1, new ScatheZombies());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        enterDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(defenderIndex(blocker), attackerIndex(free))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Awesome Presence taxes only being blocked — the enchanted creature attacks for free")
    void attackingWithTheEnchantedCreatureIsFree() {
        harness.setLife(player2, 20);
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        enchant(enchanted, player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // player1's pool is empty: reading the BE_BLOCKED_BY tax as ATTACK would reject this
        gs.declareAttackers(gd, player1, List.of(attackerIndex(enchanted)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Awesome Presence taxes only being blocked — the enchanted creature blocks for free")
    void blockingWithTheEnchantedCreatureIsFree() {
        Permanent attacker = attacking(player1, new ScatheZombies());
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        enchant(enchanted, player1);
        enterDeclareBlockers();

        // player2's pool is empty: reading the BE_BLOCKED_BY tax as BLOCK_WITH would reject this
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(defenderIndex(enchanted), attackerIndex(attacker))));

        assertThat(enchanted.isBlocking()).isTrue();
    }
}
