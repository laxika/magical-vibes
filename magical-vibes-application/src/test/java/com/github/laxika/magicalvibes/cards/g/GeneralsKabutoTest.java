package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneralsKabutoTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has shroud")
    void equippedCreatureHasShroud() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addKabutoAttached(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature can't be targeted by an opponent's spell")
    void equippedCreatureCannotBeTargeted() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addKabutoAttached(player1, creature);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature loses shroud when the Kabuto is removed")
    void creatureLosesShroudWhenKabutoRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent kabuto = addKabutoAttached(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(kabuto);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Combat damage dealt to the equipped creature is prevented")
    void combatDamageToEquippedCreatureIsPrevented() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        addKabutoAttached(player1, blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Air Elemental's 4 damage would kill a 2/2, but it is prevented.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The equipped creature still deals its own combat damage")
    void equippedCreatureStillDealsCombatDamage() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        addKabutoAttached(player1, blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage to the equipped creature is not prevented")
    void noncombatDamageIsNotPrevented() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addKabutoAttached(player1, creature);
        // Pyroclasm doesn't target, so shroud doesn't stop it, and its damage isn't combat damage.
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage is no longer prevented once the Kabuto is unattached")
    void preventionStopsWhenUnattached() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent kabuto = addKabutoAttached(player1, blocker);
        kabuto.setAttachedTo(null);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving equip attaches the Kabuto to target creature")
    void resolvingEquipAttaches() {
        Permanent kabuto = addKabutoReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(kabuto.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addKabutoReady(Player player) {
        Permanent perm = new Permanent(new GeneralsKabuto());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addKabutoAttached(Player player, Permanent creature) {
        Permanent perm = addKabutoReady(player);
        perm.setAttachedTo(creature.getId());
        return perm;
    }
}
