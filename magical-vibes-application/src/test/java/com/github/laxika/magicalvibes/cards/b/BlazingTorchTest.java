package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

class BlazingTorchTest extends BaseCardTest {

    // ===== Equip =====

    @Test
    @DisplayName("Resolving equip ability attaches Blazing Torch to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent torch = addTorchReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(torch.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Granted ability: deal 2 damage to creature =====

    @Test
    @DisplayName("Equipped creature can tap and sacrifice Blazing Torch to deal 2 damage to target creature")
    void grantedAbilityDeals2DamageToCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        Permanent targetCreature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        // Grizzly Bears has 2 toughness, 2 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(targetCreature.getId()));

        // The equipped creature should be tapped but still alive
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Blazing Torch should be sacrificed (gone from battlefield)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blazing Torch"));
    }

    // ===== Granted ability: deal 2 damage to player =====

    @Test
    @DisplayName("Equipped creature can tap and sacrifice Blazing Torch to deal 2 damage to a player")
    void grantedAbilityDeals2DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(creature.isTapped()).isTrue();

        // Blazing Torch should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blazing Torch"));
    }

    // ===== Creature survives sacrifice of equipment =====

    @Test
    @DisplayName("Equipped creature stays on battlefield after Blazing Torch is sacrificed")
    void creatureStaysAfterTorchSacrificed() {
        Permanent creature = addReadyCreature(player1);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    // ===== Blazing Torch goes to graveyard =====

    @Test
    @DisplayName("Blazing Torch goes to graveyard when sacrificed")
    void torchGoesToGraveyard() {
        Permanent creature = addReadyCreature(player1);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blazing Torch"));
    }

    // ===== Summoning sickness prevents activation =====

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Already tapped creature cannot activate =====

    @Test
    @DisplayName("Already tapped creature cannot use granted tap ability")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();

        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Ability lost when equipment removed =====

    @Test
    @DisplayName("Creature loses granted ability when Blazing Torch is removed")
    void creatureLosesAbilityWhenTorchRemoved() {
        Permanent creature = addReadyCreature(player1);

        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        // Remove Blazing Torch
        gd.playerBattlefields.get(player1.getId()).remove(torch);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Blocking restriction: can't be blocked by Vampires =====

    @Test
    @DisplayName("Equipped creature can't be blocked by Vampires")
    void equippedCreatureCantBeBlockedByVampires() {
        Permanent creature = addReadyCreature(player1);
        creature.setAttacking(true);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        Permanent vampire = new Permanent(createSubtypeCreature("Test Vampire", CardSubtype.VAMPIRE));
        vampire.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(vampire);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    // ===== Blocking restriction: can't be blocked by Zombies =====

    @Test
    @DisplayName("Equipped creature can't be blocked by Zombies")
    void equippedCreatureCantBeBlockedByZombies() {
        Permanent creature = addReadyCreature(player1);
        creature.setAttacking(true);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        Permanent zombie = new Permanent(createSubtypeCreature("Test Zombie", CardSubtype.ZOMBIE));
        zombie.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(zombie);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    // ===== Damage source is Blazing Torch, not equipped creature =====

    @Test
    @DisplayName("Damage source is Blazing Torch, not the equipped creature — damage log attributes to Blazing Torch")
    void damageSourceIsTorchNotCreature() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Damage log must attribute to "Blazing Torch" (the equipment), not "Grizzly Bears" (the creature)
        assertThat(gd.gameLog).anyMatch(log -> log.contains("damage from Blazing Torch"));
        assertThat(gd.gameLog).noneMatch(log -> log.contains("damage from Grizzly Bears"));
    }

    // ===== Non-Vampire non-Zombie can still block =====

    @Test
    @DisplayName("Equipped creature can be blocked by non-Vampire non-Zombie creatures")
    void equippedCreatureCanBeBlockedByNormalCreatures() {
        Permanent creature = addReadyCreature(player1);
        creature.setAttacking(true);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Helpers =====

    private Permanent addTorchReady(Player player) {
        Permanent perm = new Permanent(new BlazingTorch());
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

    private Card createSubtypeCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
