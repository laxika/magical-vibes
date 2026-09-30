package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HedgeTroll.class, Plains.class})
class HedgeTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls a Plains")
    void getsBonusWhileControllerControlsPlains() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new HedgeTroll());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not get the bonus without a Plains")
    void noBonusWithoutPlains() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new HedgeTroll());

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Plains does not grant the bonus")
    void opponentPlainsDoesNotGrantBonus() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new HedgeTroll());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(2);
    }

    @Test
    @DisplayName("An additional Plains does not increase the bonus")
    void additionalPlainsDoNotStackBonus() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new HedgeTroll());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
    }

    @Test
    @DisplayName("Paying {W} grants a regeneration shield")
    void whiteActivationGrantsRegenerationShield() {
        Permanent troll = addCreatureReady(player1, new HedgeTroll());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate regeneration while tapped")
    void regenerationCanBeActivatedWhileTapped() {
        Permanent troll = addCreatureReady(player1, new HedgeTroll());
        troll.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Hedge Troll from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent troll = addCreatureReady(player1, new HedgeTroll());
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new HedgeTroll());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Hedge Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
    }
}
