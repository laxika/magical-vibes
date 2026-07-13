package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }

    @Test
    @DisplayName("Sacrifices itself and destroys the attacking non-flying creature")
    void destroysAttackingNonFlyer() {
        Permanent trap = addReadyTrap(player1);
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, idxOf(player1, trap), 0, null, attacker.getId());
        harness.passBothPriorities();

        // Pit Trap sacrificed as a cost
        harness.assertNotOnBattlefield(player1, "Pit Trap");
        harness.assertInGraveyard(player1, "Pit Trap");

        // Target destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetFlyingAttacker() {
        Permanent trap = addReadyTrap(player1);
        Permanent flyer = addAttacker(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, idxOf(player1, trap), 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        Permanent trap = addReadyTrap(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, idxOf(player1, trap), 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
