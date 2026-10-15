package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldOfTheRealm.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class ShieldOfTheRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Equip cannot be activated without paying its mana cost")
    void cannotEquipWithoutMana() {
        addShieldReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Resolving equip attaches Shield of the Realm to target creature")
    void resolvingEquipAttaches() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prevents 2 of 3 noncombat damage to equipped creature")
    void prevents2Of3NoncombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        // Lightning Bolt deals 3 damage to the equipped creature
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 3 - 2 prevented = 1 damage taken; creature (2/2) survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents all damage when source deals 2 or less")
    void preventsAllDamageWhenSourceDeals2OrLess() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        // Shock deals 2 damage to the equipped creature
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 2 - 2 prevented = 0 damage
        assertThat(creature.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Prevents 2 combat damage to equipped creature from each attacker")
    void prevents2CombatDamageFromEachSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player1 has a creature equipped with Shield of the Realm
        Permanent defender = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(defender.getId());

        // Player2 attacks with a 2/2
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        // Defender blocks
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Attacker deals 2 combat damage - 2 prevented = 0 damage
        assertThat(defender.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not prevent damage to unequipped creature")
    void doesNotPreventDamageToUnequippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addShieldReady(player1); // Shield on battlefield but not attached

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 2/2 creature takes full 3 damage and dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Two Shields of the Realm prevent 4 damage total per source")
    void twoShieldsPrevent4Damage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield1 = addShieldReady(player1);
        Permanent shield2 = addShieldReady(player1);
        shield1.setAttachedTo(creature.getId());
        shield2.setAttachedTo(creature.getId());

        // Lightning Bolt deals 3 damage to the equipped creature
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 3 - 4 prevented = 0 damage (clamped at 0)
        assertThat(creature.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Moving Shield of the Realm transfers prevention to new creature")
    void reEquipTransfersPrevention() {
        Permanent shield = addShieldReady(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        shield.setAttachedTo(creature1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature2.getId());

        // Now Lightning Bolt creature2 — should be prevented
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature2.getId());
        harness.passBothPriorities();

        // 3 - 2 = 1 damage to creature2
        assertThat(creature2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip requires sorcery timing")
    void cannotEquipDuringOpponentsTurn() {
        addShieldReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Prevention applies again to successive damage events")
    void preventsDamageFromSuccessiveSpells() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An opponent-controlled Shield still protects the equipped creature")
    void preventsDamageRegardlessOfEquipmentController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player2);
        shield.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addShieldReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ShieldOfTheRealm());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
