package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinSpelunkers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PitTrap.class, GoblinSpelunkers.class, PegasusCharger.class})
class PitTrapTest extends BaseCardTest {

    private Permanent addReadyTrap(Player player) {
        Permanent trap = new Permanent(new PitTrap());
        trap.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(trap);
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
        Permanent flyer = addAttacker(player2, new PegasusCharger());
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
}
