package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarpoonSniper.class, GoldmeadowStalwart.class, DeeptreadMerrow.class})
class HarpoonSniperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps the sniper and puts it on the stack")
    void activatingPutsOnStack() {
        Permanent sniper = addSniperReady(player1);
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(sniper.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("With a single Merfolk, deals 1 damage — 2-toughness target survives")
    void dealsOneWithSingleMerfolk() {
        addSniperReady(player1);
        addCreatureReady(player2, new DeeptreadMerrow());
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Goldmeadow Stalwart");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("deals 1 damage"));
    }

    @Test
    @DisplayName("X scales with Merfolk count — two Merfolk destroys a 2-toughness target")
    void dealsDamageEqualToMerfolkCount() {
        addSniperReady(player1);
        addCreatureReady(player1, new DeeptreadMerrow());
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Goldmeadow Stalwart");
        harness.assertInGraveyard(player2, "Goldmeadow Stalwart");
    }

    @Test
    @DisplayName("Counts Merfolk controlled when the ability resolves")
    void countsMerfolkAtResolution() {
        addSniperReady(player1);
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        addCreatureReady(player1, new DeeptreadMerrow());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goldmeadow Stalwart");
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void dealsDamageToBlockingCreature() {
        Permanent sniper = addSniperReady(player1);
        Permanent attacker = addCreatureReady(player1, new GoldmeadowStalwart());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GoldmeadowStalwart());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(sniper.isTapped()).isTrue();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not damage a target that stops attacking before resolution")
    void targetMustStillBeAttackingWhenAbilityResolves() {
        addSniperReady(player1);
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addSniperReady(player1);
        Permanent bystander = addCreatureReady(player2, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Ability still deals damage after the sniper leaves, counting only remaining Merfolk")
    void sourceLeavingDoesNotStopAbility() {
        Permanent sniper = addSniperReady(player1);
        addCreatureReady(player1, new DeeptreadMerrow());
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sniper);
        gd.playerGraveyards.get(player1.getId()).add(sniper.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Goldmeadow Stalwart");
    }

    @Test
    @DisplayName("Deals no damage when no Merfolk remain at resolution")
    void noRemainingMerfolkMeansZeroDamage() {
        Permanent sniper = addSniperReady(player1);
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sniper);
        gd.playerGraveyards.get(player1.getId()).add(sniper.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Goldmeadow Stalwart");
    }

    @Test
    @DisplayName("Can target an attacking creature you control")
    void canTargetOwnAttacker() {
        addSniperReady(player1);
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A blocking target must still be in combat at resolution")
    void blockingTargetLeavingCombatBecomesIllegal() {
        addSniperReady(player1);
        addAttackingCreature(player1);
        Permanent blocker = addCreatureReady(player2, new GoldmeadowStalwart());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void requiresWhiteMana() {
        Permanent sniper = addSniperReady(player1);
        Permanent attacker = addAttackingCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sniper.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void requiresUntappedSource() {
        Permanent sniper = addSniperReady(player1);
        sniper.tap();
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void summoningSicknessPreventsActivation() {
        Permanent sniper = addSniperReady(player1);
        sniper.setSummoningSick(true);
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sniper.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSniperReady(Player player) {
        return addCreatureReady(player, new HarpoonSniper());
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new GoldmeadowStalwart());
        creature.setAttacking(true);
        return creature;
    }
}
