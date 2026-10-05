package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinSpelunkers;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinSpelunkers.class, WindDrake.class, PitTrap.class})
class PitTrapTest extends BaseCardTest {

    private Permanent addReadyTrap(Player player) {
        Permanent trap = harness.addToBattlefieldAndReturn(player, new PitTrap());
        trap.setSummoningSick(false);
        return trap;
    }

    private int idxOf(Player player, Permanent p) {
        return gd.playerBattlefields.get(player.getId()).indexOf(p);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    @Test
    @DisplayName("Sacrifices itself and destroys the attacking non-flying creature")
    void destroysAttackingNonFlyer() {
        Permanent trap = addReadyTrap(player1);
        Permanent attacker = addAttacker(player2, new GoblinSpelunkers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId());
        harness.passBothPriorities();

        // Pit Trap sacrificed as a cost
        harness.assertNotOnBattlefield(player1, "Pit Trap");
        harness.assertInGraveyard(player1, "Pit Trap");

        // Target destroyed
        harness.assertNotOnBattlefield(player2, "Goblin Spelunkers");
        harness.assertInGraveyard(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("Destroys an attacking creature despite a regeneration shield")
    void cannotBeRegenerated() {
        Permanent trap = addReadyTrap(player1);
        Permanent attacker = addAttacker(player2, new GoblinSpelunkers());
        attacker.setRegenerationShield(1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Spelunkers");
        harness.assertInGraveyard(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetFlyingAttacker() {
        Permanent trap = addReadyTrap(player1);
        Permanent flyer = addAttacker(player2, new WindDrake());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, idxOf(player1, trap), 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        Permanent trap = addReadyTrap(player1);
        harness.addToBattlefield(player2, new GoblinSpelunkers());
        var targetId = harness.getPermanentId(player2, "Goblin Spelunkers");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, idxOf(player1, trap), 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        Permanent trap = addReadyTrap(player1);
        Permanent attacker = addAttacker(player2, new GoblinSpelunkers());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pit Trap");
        harness.assertOnBattlefield(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("Does not destroy the target if it stops attacking before resolution")
    void targetMustStillBeAttackingOnResolution() {
        Permanent trap = addReadyTrap(player1);
        Permanent attacker = addAttacker(player2, new GoblinSpelunkers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pit Trap");
        harness.assertOnBattlefield(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("Requires two mana to activate")
    void requiresTwoMana() {
        Permanent trap = addReadyTrap(player1);
        Permanent attacker = addAttacker(player2, new GoblinSpelunkers());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pit Trap");
        harness.assertOnBattlefield(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("Requires the artifact to be untapped")
    void requiresUntappedSource() {
        Permanent trap = addReadyTrap(player1);
        trap.tap();
        Permanent attacker = addAttacker(player2, new GoblinSpelunkers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pit Trap");
        harness.assertOnBattlefield(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("Does not destroy a target that gains flying before resolution")
    void targetGainingFlyingBecomesIllegal() {
        Permanent trap = addReadyTrap(player1);
        Permanent attacker = addAttacker(player2, new GoblinSpelunkers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId());
        attacker.getGrantedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pit Trap");
        harness.assertOnBattlefield(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("Cannot destroy an indestructible attacking creature")
    void indestructibleAttackerSurvives() {
        Permanent trap = addReadyTrap(player1);
        Permanent attacker = addAttacker(player2, new GoblinSpelunkers());
        attacker.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pit Trap");
        harness.assertOnBattlefield(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("A newly entered noncreature trap can activate and is sacrificed immediately")
    void newlyEnteredTrapPaysSacrificeBeforeResolution() {
        Permanent trap = harness.addToBattlefieldAndReturn(player1, new PitTrap());
        trap.setSummoningSick(true);
        Permanent attacker = addAttacker(player2, new GoblinSpelunkers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Pit Trap");
        harness.assertInGraveyard(player1, "Pit Trap");
        harness.assertOnBattlefield(player2, "Goblin Spelunkers");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("Can destroy its controller's own attacking creature")
    void canTargetOwnAttacker() {
        Permanent trap = addReadyTrap(player1);
        Permanent attacker = addCreatureReady(player1, new GoblinSpelunkers());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pit Trap");
        harness.assertInGraveyard(player1, "Goblin Spelunkers");
    }
}
