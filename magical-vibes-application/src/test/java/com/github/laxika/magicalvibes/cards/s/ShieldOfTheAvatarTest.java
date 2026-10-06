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

@CardUsed({ShieldOfTheAvatar.class, GrizzlyBears.class, LightningBolt.class})
class ShieldOfTheAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 of 3 damage when the equipped creature is the only creature you control")
    void preventsOnePerCreatureControlled() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        // 3 - 1 prevented = 2 damage, lethal to a 2/2
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevention scales with the number of creatures you control")
    void preventionScalesWithCreatureCount() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        // 3 creatures controlled → all 3 damage prevented
        assertThat(creature.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Creatures the opponent controls do not increase the prevention")
    void opponentCreaturesDoNotCount() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        // Still only 1 prevented → 2 damage kills the 2/2
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not prevent damage while unattached")
    void doesNotPreventWhileUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addShieldReady(player1);

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equip {2} attaches the Shield to target creature you control")
    void equipAttaches() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage separately from each simultaneous combat source")
    void preventsDamageFromEachBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        for (Permanent blocker : List.of(firstBlocker, secondBlocker)) {
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            blocker.addBlockingTargetId(attacker.getId());
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("An Equipment that has lost its abilities does not prevent damage")
    void doesNotPreventWhenShieldLosesAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());
        shield.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Uses the Equipment controller's creatures when attached to an opponent's creature")
    void countsEquipmentControllersCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Reevaluates creature count for each damage event and does not consume prevention")
    void reevaluatesCountForRepeatedDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent support = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, support.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(support);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addShieldReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ShieldOfTheAvatar());
    }
}
