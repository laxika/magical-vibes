package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarrowBatsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 4 life grants a regeneration shield")
    void payLifeGrantsRegenerationShield() {
        Permanent bats = addCreatureReady(player1, new MarrowBats());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(bats.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with less than 4 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new MarrowBats());
        harness.setLife(player1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Regeneration shield saves Marrow Bats from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent bats = addCreatureReady(player1, new MarrowBats());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        bats.setBlocking(true);
        bats.addBlockingTarget(0);

        Permanent attacker = addAttacker(player2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Marrow Bats");
        assertThat(bats.isTapped()).isTrue();
        assertThat(bats.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Marrow Bats dies to lethal combat damage without a shield")
    void diesWithoutShield() {
        Permanent bats = addCreatureReady(player1, new MarrowBats());
        bats.setBlocking(true);
        bats.addBlockingTarget(0);

        Permanent attacker = addAttacker(player2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Marrow Bats");
        harness.assertInGraveyard(player1, "Marrow Bats");
    }

    private Permanent addAttacker(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(5);
        card.setToughness(5);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
