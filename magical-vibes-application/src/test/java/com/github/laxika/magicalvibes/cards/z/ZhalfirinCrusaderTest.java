package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CryptRats;
import com.github.laxika.magicalvibes.cards.d.DarajaGriffin;
import com.github.laxika.magicalvibes.cards.d.DwarvenVigilantes;
import com.github.laxika.magicalvibes.cards.o.OgreEnforcer;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZhalfirinCrusader.class, DarajaGriffin.class, DwarvenVigilantes.class,
        OgreEnforcer.class, CryptRats.class})
class ZhalfirinCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void flankingHitsNonFlankingBlocker() {
        Permanent crusader = addCreatureReady(player1, new ZhalfirinCrusader());
        crusader.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DarajaGriffin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking does not weaken a blocker that has flanking")
    void flankingLeavesFlankingBlockerUntouched() {
        Permanent crusader = addCreatureReady(player1, new ZhalfirinCrusader());
        crusader.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZhalfirinCrusader());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking weakens each non-flanking blocker")
    void flankingHitsEachNonFlankingBlocker() {
        Permanent crusader = addCreatureReady(player1, new ZhalfirinCrusader());
        crusader.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player2, new DarajaGriffin());
        Permanent blocker2 = addCreatureReady(player2, new DarajaGriffin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(indexOf(player2, blocker1), indexOf(player1, crusader)),
                new BlockerAssignment(indexOf(player2, blocker2), indexOf(player1, crusader))));
        resolveAllTriggers();

        assertThat(blocker1.getEffectivePower()).isEqualTo(1);
        assertThat(blocker1.getEffectiveToughness()).isEqualTo(1);
        assertThat(blocker2.getEffectivePower()).isEqualTo(1);
        assertThat(blocker2.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking's -1/-1 effect wears off at end of turn")
    void flankingWearsOffAtEndOfTurn() {
        Permanent crusader = addCreatureReady(player1, new ZhalfirinCrusader());
        crusader.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DarajaGriffin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating the ability registers an amount-limited redirect shield protecting itself")
    void activationCreatesShield() {
        Permanent crusader = addCreatureReady(player1, new ZhalfirinCrusader());
        Permanent destination = addCreatureReady(player2, new OgreEnforcer());

        addCrusaderActivationMana(player1);
        harness.activateAbility(player1, indexOf(player1, crusader), null, destination.getId());
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        var shield = gd.creatureDamageRedirectShields.getFirst();
        assertThat(shield.protectedPermanentId()).isEqualTo(crusader.getId());
        assertThat(shield.damageSourceId()).isNull();
        assertThat(shield.remainingAmount()).isEqualTo(1);
        assertThat(shield.redirectTargetId()).isEqualTo(destination.getId());
    }

    @Test
    @DisplayName("Noncombat damage to Zhalfirin Crusader is redirected to the target creature")
    void redirectsNoncombatDamageToCreature() {
        Permanent crusader = addCreatureReady(player1, new ZhalfirinCrusader());
        Permanent rats = addCreatureReady(player2, new CryptRats());
        Permanent destination = addCreatureReady(player2, new OgreEnforcer());

        addCrusaderActivationMana(player1);
        harness.activateAbility(player1, indexOf(player1, crusader), null, destination.getId());
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, indexOf(player2, rats), 1, null);
        harness.passBothPriorities();

        assertThat(crusader.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage can be redirected to a player")
    void redirectsDamageToPlayer() {
        Permanent crusader = addCreatureReady(player1, new ZhalfirinCrusader());
        Permanent rats = addCreatureReady(player2, new CryptRats());
        int lifeBefore = gd.getLife(player2.getId());

        addCrusaderActivationMana(player1);
        harness.activateAbility(player1, indexOf(player1, crusader), null, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, indexOf(player2, rats), 1, null);
        harness.passBothPriorities();

        assertThat(crusader.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Only the next 1 damage is redirected; the rest still lands on the Crusader")
    void redirectsOnlyOneDamage() {
        Permanent crusader = addCreatureReady(player1, new ZhalfirinCrusader());
        Permanent destination = addCreatureReady(player1, new OgreEnforcer());
        Permanent attacker = addCreatureReady(player2, new DwarvenVigilantes());

        addCrusaderActivationMana(player1);
        harness.activateAbility(player1, indexOf(player1, crusader), null, destination.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, crusader), 0)));
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(crusader.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent crusader = addCreatureReady(player1, new ZhalfirinCrusader());
        Permanent destination = addCreatureReady(player2, new OgreEnforcer());

        addCrusaderActivationMana(player1);
        harness.activateAbility(player1, indexOf(player1, crusader), null, destination.getId());
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).isEmpty();
    }

    private void addCrusaderActivationMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
