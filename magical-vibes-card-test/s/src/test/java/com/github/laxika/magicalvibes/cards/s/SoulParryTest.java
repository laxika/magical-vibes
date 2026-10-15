package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.m.MoriokReplica;
import com.github.laxika.magicalvibes.cards.v.VulshokReplica;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
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

@CardUsed({SoulParry.class, MoriokReplica.class, SpikeshotElder.class,
        VulshokReplica.class, LeylineOfPunishment.class})
class SoulParryTest extends BaseCardTest {

    @Test
    @DisplayName("Single target creature is prevented from dealing damage")
    void singleTargetPrevented() {
        Permanent bear = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(bear.getId());
    }

    @Test
    @DisplayName("Two target creatures are both prevented from dealing damage")
    void twoTargetsPrevented() {
        Permanent bear1 = addCreature(player2);
        Permanent bear2 = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bear1.getId(), bear2.getId()));

        assertThat(gd.permanentsPreventedFromDealingDamage)
                .contains(bear1.getId(), bear2.getId());
    }

    @Test
    @DisplayName("Prevented creature deals no combat damage to player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Cast Soul Parry targeting the attacker
        harness.castAndResolveInstant(player1, 0, List.of(attacker.getId()));

        // Set up combat — attacker is unblocked
        attacker.setAttacking(true);
        resolveCombat(player2);

        // Player should not have taken damage
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevented creature deals no combat damage to blocking creature")
    void preventsCombatDamageToCreature() {
        Permanent attacker = addCreature(player2); // Moriok Replica 2/2
        Permanent blocker = addCreature(player1);   // Moriok Replica 2/2
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Cast Soul Parry targeting the attacker
        harness.castAndResolveInstant(player1, 0, List.of(attacker.getId()));

        // Set up combat with blocking
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        // Blocker should survive (attacker's damage prevented), attacker should take blocker's damage
        harness.assertOnBattlefield(player1, "Moriok Replica");
    }

    @Test
    @DisplayName("Spell partially resolves when one of two targets is removed")
    void partiallyResolvesWhenOneTargetRemoved() {
        Permanent bear1 = addCreature(player2);
        Permanent bear2 = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(bear1.getId(), bear2.getId()));

        // Remove first target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(bear1);

        harness.passBothPriorities();

        // Only the remaining target should be prevented
        assertThat(gd.permanentsPreventedFromDealingDamage)
                .doesNotContain(bear1.getId())
                .contains(bear2.getId());
    }

    @Test
    @DisplayName("Spell fizzles when all targets are removed")
    void fizzlesWhenAllTargetsRemoved() {
        Permanent bear = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(bear.getId()));

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        Permanent bear = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(bear.getId());

        // Advance past end of turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
    }

    @Test
    @DisplayName("Soul Parry goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        Permanent bear = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        harness.assertInGraveyard(player1, "Soul Parry");
    }

    @Test
    @DisplayName("Prevention stops repeated noncombat damage to players and creatures")
    void preventsRepeatedNoncombatDamage() {
        Permanent elder = addCreatureReady(player2, new SpikeshotElder());
        Permanent recipient = addCreature(player1);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(elder.getId()));

        harness.addMana(player2, ManaColor.RED, 6);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, recipient.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(recipient.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Moriok Replica");
    }

    @Test
    @DisplayName("Prevention still applies when the creature is sacrificed to deal damage")
    void preventsDamageFromSacrificedCreature() {
        Permanent replica = harness.addToBattlefieldAndReturn(player2, new VulshokReplica());
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(replica.getId()));

        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vulshok Replica");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damage that cannot be prevented overrides Soul Parry")
    void doesNotPreventUnpreventableNoncombatDamage() {
        Permanent elder = addCreatureReady(player2, new SpikeshotElder());
        harness.addToBattlefield(player2, new LeylineOfPunishment());
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(elder.getId()));

        harness.addMana(player2, ManaColor.RED, 3);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Prevention applies to both an attacking and a blocking creature")
    void preventsDamageByCreaturesControlledByBothPlayers() {
        Permanent attacker = addCreature(player2);
        Permanent blocker = addCreature(player1);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(attacker.getId(), blocker.getId()));

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Moriok Replica");
        harness.assertOnBattlefield(player2, "Moriok Replica");
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevention remains active during the end step")
    void preventsDamageDuringEndStep() {
        Permanent elder = addCreatureReady(player2, new SpikeshotElder());
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(elder.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.addMana(player2, ManaColor.RED, 3);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Casting requires at least one target and at most two distinct targets")
    void rejectsInvalidTargetCountsAndDuplicates() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        Permanent third = addCreature(player2);
        harness.setHand(player1, List.of(new SoulParry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new MoriokReplica());
    }
}
