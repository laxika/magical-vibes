package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JoustingLanceTest extends BaseCardTest {

    // ===== Static effects: +2/+0 boost =====

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent lance = addLanceReady(player1);
        lance.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);    // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Creature loses +2/+0 when lance is removed")
    void creatureLosesBoostWhenLanceRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent lance = addLanceReady(player1);
        lance.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(lance);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    // ===== Conditional first strike: during your turn =====

    @Test
    @DisplayName("Equipped creature has first strike during controller's turn")
    void equippedCreatureHasFirstStrikeDuringYourTurn() {
        Permanent creature = addReadyCreature(player1);
        Permanent lance = addLanceReady(player1);
        lance.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does NOT have first strike during opponent's turn")
    void equippedCreatureNoFirstStrikeDuringOpponentTurn() {
        Permanent creature = addReadyCreature(player1);
        Permanent lance = addLanceReady(player1);
        lance.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike toggles when active player changes")
    void firstStrikeTogglesWithActivePlayer() {
        Permanent creature = addReadyCreature(player1);
        Permanent lance = addLanceReady(player1);
        lance.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Boost always applies regardless of turn =====

    @Test
    @DisplayName("+2/+0 boost applies during opponent's turn too")
    void boostAppliesDuringOpponentTurn() {
        Permanent creature = addReadyCreature(player1);
        Permanent lance = addLanceReady(player1);
        lance.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);    // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Moving lance transfers +2/+0 and conditional first strike to new creature")
    void movingLanceTransfersEffects() {
        Permanent lance = addLanceReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        lance.setAttachedTo(creature1.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Combat: first strike during your turn =====

    @Test
    @DisplayName("Equipped creature deals first strike combat damage on controller's turn")
    void equippedCreatureDealsFirstStrikeDamageOnYourTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent lance = addLanceReady(player1);
        lance.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat(player1);

        // 2 base + 2 from lance = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Helpers =====

    private Permanent addLanceReady(Player player) {
        Permanent perm = new Permanent(new JoustingLance());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
