package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VorracBattlehorns.class, AlphaMyr.class})
class VorracBattlehornsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Vorrac Battlehorns to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent battlehorns = addBattlehornsReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(battlehorns.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Successful equip is not logged as a fizzle")
    void successfulEquipIsNotLoggedAsFizzle() {
        addBattlehornsReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("Vorrac Battlehorns") && log.contains("fizzles"));
    }

    @Test
    @DisplayName("Equipped creature has trample and can be blocked by at most one creature")
    void equippedCreatureGainsTrampleAndBlockLimit() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent battlehorns = addBattlehornsReady(player1);
        battlehorns.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unattached Vorrac Battlehorns grant no abilities")
    void unattachedBattlehornsGrantNoAbilities() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        addBattlehornsReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Equipped creature cannot be blocked by two creatures")
    void equippedCreatureCannotBeBlockedByTwoCreatures() {
        Permanent creature = addAttackingCreature(player1);
        Permanent battlehorns = addBattlehornsReady(player1);
        battlehorns.setAttachedTo(creature.getId());
        Permanent blocker1 = addCreatureReady(player2, new AlphaMyr());
        Permanent blocker2 = addCreatureReady(player2, new AlphaMyr());

        prepareDeclareBlockers();

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
    @DisplayName("Equipped creature can be blocked by one creature")
    void equippedCreatureCanBeBlockedByOneCreature() {
        Permanent creature = addAttackingCreature(player1);
        Permanent battlehorns = addBattlehornsReady(player1);
        battlehorns.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new AlphaMyr());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equip ability can target only a creature controlled by its controller")
    void equipCannotTargetOpponentsCreature() {
        Permanent battlehorns = addBattlehornsReady(player1);
        Permanent creature = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(battlehorns.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip ability can be activated only at sorcery speed")
    void equipCannotBeActivatedOutsideSorcerySpeed() {
        Permanent battlehorns = addBattlehornsReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(battlehorns.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Removing Vorrac Battlehorns removes its granted abilities")
    void removingBattlehornsRemovesGrantedAbilities() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent battlehorns = addBattlehornsReady(player1);
        battlehorns.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(battlehorns);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(Integer.MAX_VALUE);
    }

    private Permanent addBattlehornsReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VorracBattlehorns());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Re-equipping moves both granted abilities to the new creature")
    void reequippingMovesGrantedAbilities() {
        Permanent battlehorns = addBattlehornsReady(player1);
        Permanent first = addCreatureReady(player1, new AlphaMyr());
        Permanent second = addCreatureReady(player1, new AlphaMyr());
        battlehorns.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(battlehorns.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, first)).isEqualTo(Integer.MAX_VALUE);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipped creature can assign excess combat damage to the defending player")
    void trampleDealsExcessDamageToDefender() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttackingCreature(player1);
        Permanent battlehorns = addBattlehornsReady(player1);
        battlehorns.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new AlphaMyr());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Alpha Myr");
        harness.assertInGraveyard(player2, "Alpha Myr");
        assertThat(battlehorns.getAttachedTo()).isNull();
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new AlphaMyr());
        creature.setAttacking(true);
        return creature;
    }
}
