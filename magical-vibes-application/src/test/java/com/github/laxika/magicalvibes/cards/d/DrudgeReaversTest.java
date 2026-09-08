package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrudgeReavers.class, GrizzlyBears.class})
class DrudgeReaversTest extends BaseCardTest {

    @Test
    @DisplayName("{B} grants Drudge Reavers a regeneration shield")
    void activationGrantsRegenerationShield() {
        Permanent reavers = addReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(reavers.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Drudge Reavers from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent reavers = addReady(player1);
        reavers.setRegenerationShield(1);
        reavers.setBlocking(true);
        reavers.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drudge Reavers");
        assertThat(reavers.isTapped()).isTrue();
        assertThat(reavers.getRegenerationShield()).isZero();
    }

    private Permanent addReady(Player player) {
        Permanent reavers = new Permanent(new DrudgeReavers());
        reavers.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(reavers);
        return reavers;
    }
}
