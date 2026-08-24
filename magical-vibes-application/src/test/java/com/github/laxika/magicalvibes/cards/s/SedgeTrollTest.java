package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SedgeTroll.class, Swamp.class, Forest.class, HillGiant.class})
class SedgeTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Sedge Troll is 2/2 without a Swamp")
    void baseStatsWithoutSwamp() {
        harness.addToBattlefield(player1, new SedgeTroll());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("A non-Swamp land does not grant the boost")
    void nonSwampLandDoesNotGrantBoost() {
        harness.addToBattlefield(player1, new SedgeTroll());
        harness.addToBattlefield(player1, new Forest());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Sedge Troll gets +1/+1 while its controller controls a Swamp")
    void getsBoostWithSwamp() {
        harness.addToBattlefield(player1, new SedgeTroll());
        harness.addToBattlefield(player1, new Swamp());

        assertStats(3, 3);
    }

    @Test
    @DisplayName("An opponent's Swamp does not grant the boost")
    void opponentSwampDoesNotGrantBoost() {
        harness.addToBattlefield(player1, new SedgeTroll());
        harness.addToBattlefield(player2, new Swamp());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Paying {B} grants Sedge Troll a regeneration shield")
    void payingBlackGrantsRegenerationShield() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SedgeTroll());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Sedge Troll from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new SedgeTroll());
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sedge Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
    }

    private void assertStats(int power, int toughness) {
        Permanent troll = findPermanent(player1, "Sedge Troll");
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(toughness);
    }
}
