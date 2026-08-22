package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoredArmadillo.class, Shock.class})
class ArmoredArmadilloTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability adds the Armadillo's toughness to its power")
    void activatedAbilityBoostsByToughness() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, armadillo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, armadillo)).isEqualTo(4);
    }

    @Test
    @DisplayName("The activated ability boost wears off at end of turn")
    void activatedAbilityBoostExpiresAtEndOfTurn() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, armadillo)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, armadillo)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller does not pay")
    void wardCountersUnpaidSpell() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, armadillo.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when its controller pays")
    void payingWardLetsSpellResolve() {
        Permanent armadillo = addCreatureReady(player1, new ArmoredArmadillo());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, armadillo.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(armadillo);
        harness.assertInGraveyard(player2, "Shock");
    }
}
