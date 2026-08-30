package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorealGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Snow mana grants first strike until end of turn")
    void snowManaGrantsFirstStrike() {
        Permanent griffin = addGriffinReady(player1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A snow land produces snow mana")
    void snowLandProducesSnowMana() {
        Permanent plains = new Permanent(new SnowCoveredPlains());
        plains.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(plains);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addGriffinReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent griffin = addGriffinReady(player1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addGriffinReady(Player player) {
        Permanent permanent = new Permanent(new BorealGriffin());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
