package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RecklessWaif;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WolfhuntersQuiverTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Wolfhunter's Quiver to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent quiver = addQuiverReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(quiver.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can tap to deal 1 damage to target creature")
    void grantedAbilityDeals1DamageToCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent quiver = addEquippedQuiver(player1, creature);

        Permanent targetCreature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equipped creature can tap to deal 1 damage to a player")
    void grantedAbilityDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        addEquippedQuiver(player1, creature);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Equipped creature can tap to deal 3 damage to target Werewolf creature")
    void grantedAbilityDeals3DamageToWerewolf() {
        Permanent creature = addReadyCreature(player1);
        addEquippedQuiver(player1, creature);

        Permanent werewolf = new Permanent(new RecklessWaif());
        werewolf.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(werewolf);

        harness.activateAbility(player1, 0, 1, null, werewolf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(werewolf.getId()));
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Werewolf hunter ability cannot target non-Werewolf creatures")
    void werewolfAbilityCannotTargetNonWerewolf() {
        Permanent creature = addReadyCreature(player1);
        addEquippedQuiver(player1, creature);

        Permanent targetCreature = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Werewolf creature");
    }

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap abilities")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        addEquippedQuiver(player1, creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Already tapped creature cannot use granted tap abilities")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();
        addEquippedQuiver(player1, creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Creature loses granted abilities when Wolfhunter's Quiver is removed")
    void creatureLosesAbilityWhenQuiverRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent quiver = addEquippedQuiver(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(quiver);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Damage from any-target ability is dealt by the equipped creature")
    void damageSourceIsEquippedCreature() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        addEquippedQuiver(player1, creature);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("damage from Grizzly Bears"));
        assertThat(gd.gameLog).noneMatch(log -> log.contains("damage from Wolfhunter's Quiver"));
    }

    private Permanent addQuiverReady(Player player) {
        Permanent perm = new Permanent(new WolfhuntersQuiver());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEquippedQuiver(Player player, Permanent creature) {
        Permanent quiver = addQuiverReady(player);
        quiver.setAttachedTo(creature.getId());
        return quiver;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
