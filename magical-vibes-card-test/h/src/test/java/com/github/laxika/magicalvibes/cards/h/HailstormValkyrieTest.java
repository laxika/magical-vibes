package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HailstormValkyrie.class})
class HailstormValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability requires two snow mana")
    void requiresTwoSnowMana() {
        Permanent valkyrie = addReadyValkyrie(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating gives Hailstorm Valkyrie +2/+2 until end of turn")
    void boostsSelf() {
        Permanent valkyrie = addReadyValkyrie(player1);
        addSnowMana(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent valkyrie = addReadyValkyrie(player1);
        addSnowMana(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(2);
    }

    @Test
    @DisplayName("One snow mana and one ordinary mana cannot pay the cost")
    void requiresBothManaToBeSnow() {
        Permanent valkyrie = addReadyValkyrie(player1);
        addSnowMana(1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Snow mana of different colors pays the cost and is consumed")
    void paysWithMixedColorSnowMana() {
        Permanent valkyrie = addReadyValkyrie(player1);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.BLUE, 1);
        pool.addSnowMana(ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(pool.getSnowManaTotal()).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated activations stack even while tapped and summoning sick")
    void repeatedActivationsWhileTappedAndSummoningSick() {
        Permanent valkyrie = addReadyValkyrie(player1);
        valkyrie.setSummoningSick(true);
        valkyrie.tap();
        addSnowMana(4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(6);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(2);
    }

    private Permanent addReadyValkyrie(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player, new HailstormValkyrie());
        valkyrie.setSummoningSick(false);
        return valkyrie;
    }

    private void addSnowMana(int amount) {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.BLACK, amount);
    }
}
