package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarnassidTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{G} grants Carnassid a regeneration shield")
    void activationGrantsRegenerationShield() {
        Permanent carnassid = addCarnassidReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(carnassid.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Carnassid from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent carnassid = addCarnassidReady(player1);
        carnassid.setRegenerationShield(1);
        carnassid.setBlocking(true);
        carnassid.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 6, 6);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Carnassid");
        assertThat(carnassid.isTapped()).isTrue();
        assertThat(carnassid.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Carnassid dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent carnassid = addCarnassidReady(player1);
        carnassid.setBlocking(true);
        carnassid.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 6, 6);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Carnassid");
        harness.assertInGraveyard(player1, "Carnassid");
    }

    private Permanent addCarnassidReady(Player player) {
        Permanent perm = new Permanent(new Carnassid());
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
