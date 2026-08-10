package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

class VorracBattlehornsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Vorrac Battlehorns to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent battlehorns = addBattlehornsReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(battlehorns.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature has trample and can be blocked by at most one creature")
    void equippedCreatureGainsTrampleAndBlockLimit() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent battlehorns = addBattlehornsReady(player1);
        battlehorns.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipped creature cannot be blocked by two creatures")
    void equippedCreatureCannotBeBlockedByTwoCreatures() {
        Permanent creature = addAttackingCreature(player1);
        Permanent battlehorns = addBattlehornsReady(player1);
        battlehorns.setAttachedTo(creature.getId());
        Permanent blocker1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker2 = addCreatureReady(player2, new GrizzlyBears());

        prepareBlockerDeclaration();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        int blocker1Index = gd.playerBattlefields.get(player2.getId()).indexOf(blocker1);
        int blocker2Index = gd.playerBattlefields.get(player2.getId()).indexOf(blocker2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blocker1Index, attackerIndex),
                new BlockerAssignment(blocker2Index, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Removing Vorrac Battlehorns removes its granted abilities")
    void removingBattlehornsRemovesGrantedAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent battlehorns = addBattlehornsReady(player1);
        battlehorns.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(battlehorns);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(Integer.MAX_VALUE);
    }

    private Permanent addBattlehornsReady(Player player) {
        Permanent perm = new Permanent(new VorracBattlehorns());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }

    private void prepareBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
