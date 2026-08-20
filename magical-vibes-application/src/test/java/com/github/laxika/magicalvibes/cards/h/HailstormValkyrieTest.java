package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
        pool.add(ManaColor.BLACK, amount);
        pool.addSnowMana(ManaColor.BLACK, amount);
    }
}
