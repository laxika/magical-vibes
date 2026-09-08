package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritEnDal.class, GrizzlyBears.class})
class SpiritEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Forecast grants shadow to a target creature and keeps Spirit en-Dal in hand")
    void grantsShadowFromHand() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareUpkeepAndMana();

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHADOW)).isTrue();
        harness.assertInHand(player1, "Spirit en-Dal");
    }

    @Test
    @DisplayName("Forecast can be activated only once each turn")
    void forecastOnlyOnceEachTurn() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareUpkeepAndMana();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, bears.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast is restricted to its controller's upkeep")
    void forecastOnlyDuringYourUpkeep() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This ability can only be activated during your upkeep");
        harness.assertInHand(player1, "Spirit en-Dal");
    }

    @Test
    @DisplayName("Forecast's shadow wears off at end of turn")
    void shadowWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareUpkeepAndMana();

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHADOW)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Forecast targets a creature, not a player")
    void forecastCannotTargetPlayer() {
        harness.setHand(player1, List.of(new SpiritEnDal()));
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareUpkeepAndMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareUpkeepAndMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
