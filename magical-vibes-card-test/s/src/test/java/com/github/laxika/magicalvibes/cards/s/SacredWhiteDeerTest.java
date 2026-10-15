package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JiangYanggu;
import com.github.laxika.magicalvibes.cards.m.MuYanling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SacredWhiteDeer.class, JiangYanggu.class, MuYanling.class})
class SacredWhiteDeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains four life when you control a Yanggu planeswalker")
    void gainsLifeWithYangguPlaneswalker() {
        Permanent deer = addReadyDeer(player1);
        addReadyPlaneswalker(player1, new JiangYanggu(), 4);
        harness.setLife(player1, 10);
        addMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(deer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires a Yanggu planeswalker you control")
    void requiresYangguPlaneswalkerYouControl() {
        addReadyDeer(player1);
        addReadyPlaneswalker(player1, new MuYanling(), 5);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Yanggu planeswalker");
    }

    @Test
    @DisplayName("Does not count an opponent's Yanggu planeswalker")
    void requiresYangguPlaneswalkerYouControlNotOpponent() {
        addReadyDeer(player1);
        addReadyPlaneswalker(player2, new JiangYanggu(), 4);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addReadyDeer(Player player) {
        Permanent deer = harness.addToBattlefieldAndReturn(player, new SacredWhiteDeer());
        deer.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return deer;
    }

    private Permanent addReadyPlaneswalker(Player player, Card card, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        return planeswalker;
    }

    @Test
    void cannotActivateWithoutYanggu() {
        Permanent deer = addReadyDeer(player1);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(deer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent deer = addReadyDeer(player1);
        deer.setSummoningSick(true);
        addReadyPlaneswalker(player1, new JiangYanggu(), 4);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent deer = addReadyDeer(player1);
        deer.tap();
        addReadyPlaneswalker(player1, new JiangYanggu(), 4);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayGreenRequirementWithOnlyColorlessMana() {
        Permanent deer = addReadyDeer(player1);
        addReadyPlaneswalker(player1, new JiangYanggu(), 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(deer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainsLifeEvenIfYangguLeavesBeforeResolution() {
        addReadyDeer(player1);
        Permanent yanggu = addReadyPlaneswalker(player1, new JiangYanggu(), 4);
        harness.setLife(player1, 10);
        addMana();

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(yanggu);
        gd.playerGraveyards.get(player1.getId()).add(yanggu.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }
}
