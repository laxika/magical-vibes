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

    @Test
    @DisplayName("Tapped, summoning-sick Frostwalla can use colored snow mana to pump")
    void tappedSummoningSickCreatureCanPump() {
        Permanent frostwalla = addCreatureReady(player1, new Frostwalla());
        frostwalla.setSummoningSick(true);
        frostwalla.tap();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, frostwalla)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, frostwalla)).isEqualTo(4);
        assertThat(frostwalla.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Each Frostwalla has its own activation limit, counted before resolution")
    void activationLimitsAreIndependentAndApplyBeforeResolution() {
        Permanent first = addCreatureReady(player1, new Frostwalla());
        Permanent second = addCreatureReady(player1, new Frostwalla());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Frostwalla can pump again on the opponent's next turn")
    void activationLimitResetsOnOpponentsTurn() {
        Permanent frostwalla = addCreatureReady(player1, new Frostwalla());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, frostwalla)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, frostwalla)).isEqualTo(2);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, frostwalla)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, frostwalla)).isEqualTo(4);
    }
}
