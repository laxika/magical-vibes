package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
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

class OppressiveRaysTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can attack when its controller pays {3}")
    void attacksWhenPaid() {
        Permanent creature = addReadyCreature(player1);
        enchant(creature, player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Enchanted creature can block when its controller pays {3}")
    void blocksWhenPaid() {
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);

        Permanent blocker = addReadyCreature(player2);
        enchant(blocker, player2);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Activated abilities of the enchanted creature cost {3} more")
    void activatedAbilityCostsMore() {
        Permanent spellcaster = new Permanent(new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(spellcaster);
        enchant(spellcaster, player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The attack tax cannot be paid without {3}")
    void attackTaxRequiresPayment() {
        Permanent creature = addReadyCreature(player1);
        enchant(creature, player2);

        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Oppressive Rays taxes blocking with the enchanted creature, not blocking it")
    void blockingTheEnchantedCreatureIsFree() {
        Permanent enchanted = addReadyCreature(player1);
        enchanted.setAttacking(true);
        enchant(enchanted, player2);

        Permanent blocker = addReadyCreature(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        // player2's pool is empty: reading the BLOCK_WITH tax as BE_BLOCKED_BY would reject this
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(enchanted))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void enchant(Permanent creature, Player controller) {
        Permanent aura = new Permanent(new OppressiveRays());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
