package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Keyword;
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

class IcehideTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Snow mana activates the ability, boosts the Troll, grants indestructible, and taps it")
    void activatesWithSnowMana() {
        Permanent troll = addReadyTroll(player1);
        addSnowMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(troll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires snow mana")
    void requiresSnowMana() {
        addReadyTroll(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("The boost and indestructible wear off at end of turn")
    void temporaryEffectsWearOffAtEndOfTurn() {
        Permanent troll = addReadyTroll(player1);
        addSnowMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addReadyTroll(Player player) {
        return addCreatureReady(player, new IcehideTroll());
    }

    private void addSnowMana(Player player, int amount) {
        ManaPool pool = gd.playerManaPools.get(player.getId());
        pool.add(ManaColor.GREEN, amount);
        pool.addSnowMana(ManaColor.GREEN, amount);
    }
}
