package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DutifulThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Dutiful Thrull's regeneration targets itself")
    void activatingRegenerationTargetsSelf() {
        Permanent thrull = addDutifulThrullReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(thrull.getId());
    }

    @Test
    @DisplayName("Resolving Dutiful Thrull's regeneration grants a regeneration shield")
    void resolvingRegenerationGrantsShield() {
        addDutifulThrullReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent thrull = findPermanent(player1, "Dutiful Thrull");
        assertThat(thrull.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Dutiful Thrull's regeneration shield saves it from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent thrull = addDutifulThrullReady(player1);
        thrull.setRegenerationShield(1);
        thrull.setBlocking(true);
        thrull.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dutiful Thrull");
        Permanent survivingThrull = findPermanent(player1, "Dutiful Thrull");
        assertThat(survivingThrull.isTapped()).isTrue();
        assertThat(survivingThrull.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Dutiful Thrull dies from lethal combat damage without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent thrull = addDutifulThrullReady(player1);
        thrull.setBlocking(true);
        thrull.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dutiful Thrull");
        harness.assertInGraveyard(player1, "Dutiful Thrull");
    }

    private Permanent addDutifulThrullReady(Player player) {
        Permanent perm = new Permanent(new DutifulThrull());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
