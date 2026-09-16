package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Frostwalla.class)
class FrostwallaTest extends BaseCardTest {

    @Test
    @DisplayName("Snow mana gives Frostwalla +2/+2 until end of turn")
    void snowManaBoostsFrostwalla() {
        Permanent frostwalla = addCreatureReady(player1, new Frostwalla());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, frostwalla)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, frostwalla)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addCreatureReady(player1, new Frostwalla());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Frostwalla's pump ability can be activated only once each turn")
    void pumpAbilityOncePerTurn() {
        addCreatureReady(player1, new Frostwalla());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Frostwalla's pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent frostwalla = addCreatureReady(player1, new Frostwalla());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, frostwalla)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, frostwalla)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, frostwalla)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, frostwalla)).isEqualTo(2);
    }
}
