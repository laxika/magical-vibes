package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralLynx.class, BlackKnight.class, GrizzlyBears.class})
class SpectralLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Green creature cannot deal combat damage to Spectral Lynx")
    void protectionFromGreenPreventsCombatDamage() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent lynx = new Permanent(new SpectralLynx());
        lynx.setSummoningSick(false);
        lynx.setBlocking(true);
        lynx.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(lynx);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spectral Lynx");
    }

    @Test
    @DisplayName("Black creature can deal combat damage to Spectral Lynx")
    void protectionDoesNotPreventBlackCombatDamage() {
        Permanent attacker = new Permanent(new BlackKnight());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent lynx = new Permanent(new SpectralLynx());
        lynx.setSummoningSick(false);
        lynx.setBlocking(true);
        lynx.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(lynx);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spectral Lynx");
        harness.assertInGraveyard(player2, "Spectral Lynx");
    }

    @Test
    @DisplayName("Paying {B} grants Spectral Lynx a regeneration shield")
    void blackAbilityGrantsRegenerationShield() {
        Permanent lynx = addCreatureReady(player1, new SpectralLynx());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lynx.getRegenerationShield()).isEqualTo(1);
    }
}
