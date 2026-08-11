package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FiredrinkerSatyrTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever Firedrinker Satyr is dealt damage, its controller takes that much damage")
    void damageTakenIsReflectedToController() {
        harness.addToBattlefield(player2, new FiredrinkerSatyr());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Firedrinker Satyr"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Firedrinker Satyr");
    }

    @Test
    @DisplayName("The activated ability boosts the Satyr and deals 1 damage to its controller")
    void activatedAbilityBoostsAndDealsDamage() {
        Permanent satyr = addCreatureReady(player1, new FiredrinkerSatyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The activated boost wears off at end of turn")
    void activatedBoostWearsOffAtEndOfTurn() {
        Permanent satyr = addCreatureReady(player1, new FiredrinkerSatyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(2);
    }
}
