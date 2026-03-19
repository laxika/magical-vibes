package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeurokReplicaTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Neurok Replica has correct activated ability")
    void hasCorrectProperties() {
        NeurokReplica card = new NeurokReplica();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0))
                .isInstanceOf(SacrificeSelfCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1))
                .isInstanceOf(ReturnTargetPermanentToHandEffect.class);
        assertThat(card.getActivatedAbilities().getFirst().getTargetFilter())
                .isInstanceOf(PermanentPredicateTargetFilter.class);
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability sacrifices Neurok Replica and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addReadyReplica(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Neurok Replica should be sacrificed immediately (cost)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Neurok Replica"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Neurok Replica"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Neurok Replica");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability returns target creature to its owner's hand")
    void resolvingAbilityReturnsCreatureToHand() {
        addReadyReplica(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Target creature should be back in owner's hand
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Bounced creature does not go to graveyard")
    void bouncedCreatureDoesNotGoToGraveyard() {
        addReadyReplica(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        addReadyReplica(player1);
        Permanent target = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Mana requirements =====

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyReplica(player1);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with only colorless mana (needs blue)")
    void cannotActivateWithOnlyColorlessMana() {
        addReadyReplica(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        NeurokReplica card = new NeurokReplica();
        harness.addToBattlefield(player1, card);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Target filter =====

    @Test
    @DisplayName("Cannot target non-creature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyReplica(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyReplica(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addReadyReplica(Player player) {
        NeurokReplica card = new NeurokReplica();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
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
