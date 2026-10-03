package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.m.MarkovPatrician;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlazingTorch.class, WalkingCorpse.class, AbbeyGriffin.class, MarkovPatrician.class})
class BlazingTorchTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Blazing Torch to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent torch = addTorchReady(player1);
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(torch.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can tap and sacrifice Blazing Torch to deal 2 damage to target creature")
    void grantedAbilityDeals2DamageToCreature() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        Permanent targetCreature = addCreatureReady(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        // Walking Corpse has 2 toughness, 2 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(targetCreature.getId()));

        // The equipped creature should be tapped but still alive
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Blazing Torch should be sacrificed (gone from battlefield)
        harness.assertNotOnBattlefield(player1, "Blazing Torch");
    }

    @Test
    @DisplayName("Equipped creature can tap and sacrifice Blazing Torch to deal 2 damage to a player")
    void grantedAbilityDeals2DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(creature.isTapped()).isTrue();

        // Blazing Torch should be sacrificed
        harness.assertNotOnBattlefield(player1, "Blazing Torch");
    }

    @Test
    @DisplayName("Equipped creature stays on battlefield after Blazing Torch is sacrificed")
    void creatureStaysAfterTorchSacrificed() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Blazing Torch goes to graveyard when sacrificed")
    void torchGoesToGraveyard() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blazing Torch");
    }

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Already tapped creature cannot use granted tap ability")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        creature.tap();

        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Creature loses granted ability when Blazing Torch is removed")
    void creatureLosesAbilityWhenTorchRemoved() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());

        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        // Remove Blazing Torch
        gd.playerBattlefields.get(player1.getId()).remove(torch);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Equipped creature can't be blocked by Vampires")
    void equippedCreatureCantBeBlockedByVampires() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        creature.setAttacking(true);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        addCreatureReady(player2, new MarkovPatrician());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Equipped creature can't be blocked by Zombies")
    void equippedCreatureCantBeBlockedByZombies() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        creature.setAttacking(true);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        addCreatureReady(player2, new WalkingCorpse());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Damage source is Blazing Torch, not the equipped creature — damage log attributes to Blazing Torch")
    void damageSourceIsTorchNotCreature() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Damage log must attribute to "Blazing Torch" (the equipment), not "Walking Corpse" (the creature)
        assertThat(gameLogContains("damage from Blazing Torch")).isTrue();
        assertThat(gameLogContains("damage from Walking Corpse")).isFalse();
    }

    @Test
    @DisplayName("Equipped creature can be blocked by non-Vampire non-Zombie creatures")
    void equippedCreatureCanBeBlockedByNormalCreatures() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        creature.setAttacking(true);
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        addCreatureReady(player2, new AbbeyGriffin());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Cannot sacrifice a Torch controlled by the opponent")
    void cannotActivateWithOpponentsTorch() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent torch = addTorchReady(player2);
        torch.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Blazing Torch");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Torch damage does not use the equipped creature's lifelink")
    void torchDamageDoesNotGainLifeFromEquippedCreature() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new MarkovPatrician());
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Torch is sacrificed as a cost and its ability survives removal of the creature")
    void damageResolvesAfterEquippedCreatureLeaves() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Blazing Torch");
        harness.assertNotOnBattlefield(player1, "Blazing Torch");
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addTorchReady(Player player) {
        return addCreatureReady(player, new BlazingTorch());
    }

}
