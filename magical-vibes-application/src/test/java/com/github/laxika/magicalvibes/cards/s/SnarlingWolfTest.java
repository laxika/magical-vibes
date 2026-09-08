package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SnarlingWolf.class)
class SnarlingWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives Snarling Wolf +2/+2 until end of turn")
    void abilityBoostsSelf() {
        Permanent wolf = addReadyWolf(player1);
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent wolf = addReadyWolf(player1);
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(0);
        assertThat(wolf.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated only once each turn")
    void abilityCanBeActivatedOnlyOnceEachTurn() {
        addReadyWolf(player1);
        addMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    private Permanent addReadyWolf(Player player) {
        Permanent wolf = new Permanent(new SnarlingWolf());
        wolf.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wolf);
        return wolf;
    }

    private void addMana(Player player, int amount) {
        harness.addMana(player, ManaColor.COLORLESS, amount);
        harness.addMana(player, ManaColor.GREEN, amount);
    }
}
