package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SylvokReplicaTest extends BaseCardTest {

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability sacrifices Sylvok Replica and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Sylvok Replica should be sacrificed immediately (cost)
        harness.assertNotOnBattlefield(player1, "Sylvok Replica");
        harness.assertInGraveyard(player1, "Sylvok Replica");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Sylvok Replica");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability destroys target artifact")
    void resolvingAbilityDestroysTargetArtifact() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Resolving ability destroys target enchantment")
    void resolvingAbilityDestroysTargetEnchantment() {
        addReadyReplica(player1);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can target own artifact")
    void canTargetOwnArtifact() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    // ===== Mana requirements =====

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with only colorless mana (needs green)")
    void cannotActivateWithOnlyColorlessMana() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        SylvokReplica card = new SylvokReplica();
        harness.addToBattlefield(player1, card);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Target filter =====

    @Test
    @DisplayName("Cannot target creature")
    void cannotTargetCreature() {
        addReadyReplica(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target land")
    void cannotTargetLand() {
        addReadyReplica(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Leonin Scimitar"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addReadyReplica(Player player) {
        SylvokReplica card = new SylvokReplica();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        LeoninScimitar card = new LeoninScimitar();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyEnchantment(Player player) {
        GloriousAnthem card = new GloriousAnthem();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        Island card = new Island();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
