package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.p.PrideOfTheClouds;
import com.github.laxika.magicalvibes.cards.v.VisionSkeins;
import com.github.laxika.magicalvibes.cards.w.Windreaver;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrandArbiterAugustinIV.class, AzoriusSignet.class, MistralCharger.class, VisionSkeins.class,
        Windreaver.class, PrideOfTheClouds.class})
class GrandArbiterAugustinIVTest extends BaseCardTest {

    @Test
    @DisplayName("White spells you cast cost {1} less")
    void whiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.castFromHand(player1, new MistralCharger(), "{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Blue spells you cast cost {1} less")
    void blueSpellsCostOneLess() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.castFromHand(player1, new VisionSkeins(), "{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("White-blue spells get both cost reductions")
    void whiteBlueSpellsGetBothReductions() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.castFromHand(player1, new Windreaver(), "{1}{W}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Colorless spells do not get the color reductions")
    void colorlessSpellsDoNotGetColorReductions() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());

        assertThatThrownBy(() -> harness.castFromHand(player1, new AzoriusSignet(), "{1}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells opponents cast cost {1} more")
    void opponentSpellsCostOneMore() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new VisionSkeins(), "{1}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An opponent can cast a blue spell by paying the extra generic mana")
    void opponentCanPaySpellTax() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.castFromHand(player2, new VisionSkeins(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The opponent tax also applies to colorless spells")
    void opponentColorlessSpellIsTaxed() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new AzoriusSignet(), "{2}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Reductions cannot pay colored mana requirements")
    void reductionsDoNotRemoveColoredCosts() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());

        assertThatThrownBy(() -> harness.castFromHand(player1, new PrideOfTheClouds(), "{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A spell with no generic mana still costs its colored mana")
    void excessReductionDoesNotPreventCasting() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.castFromHand(player1, new PrideOfTheClouds(), "{W}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Both color reductions can offset an opposing Arbiter's tax")
    void reductionsOffsetOpposingTaxBeforeGenericCostIsFloored() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.addToBattlefield(player2, new GrandArbiterAugustinIV());
        harness.castFromHand(player1, new PrideOfTheClouds(), "{W}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An Arbiter in hand does not reduce its own casting cost")
    void arbiterDoesNotReduceItsOwnCost() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new GrandArbiterAugustinIV(), "{W}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Your colorless spells remain payable at their printed cost")
    void controllerColorlessSpellIsNotTaxed() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.castFromHand(player1, new AzoriusSignet(), "{2}");

        assertThat(gd.stack).hasSize(1);
    }
}
