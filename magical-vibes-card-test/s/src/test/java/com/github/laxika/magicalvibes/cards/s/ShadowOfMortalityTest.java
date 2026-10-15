package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowOfMortality.class})
class ShadowOfMortalityTest extends BaseCardTest {

    @Test
    void doesNotReduceCostAtStartingLife() {
        harness.setHand(player1, List.of(new ShadowOfMortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void reducesGenericCostByMissingLife() {
        harness.setLife(player1, 14);
        harness.castFromHand(player1, new ShadowOfMortality(), "{7}{B}{B}");
    }

    @Test
    void reductionCannotBecomeNegativeAboveStartingLife() {
        harness.setLife(player1, 21);
        harness.setHand(player1, List.of(new ShadowOfMortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void requiresAllRemainingGenericMana() {
        harness.setLife(player1, 14);
        harness.setHand(player1, List.of(new ShadowOfMortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void castsForTwoBlackWhenReductionExceedsGenericCost() {
        harness.setLife(player1, 1);
        harness.castFromHand(player1, new ShadowOfMortality(), "{B}{B}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shadow of Mortality");
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new ShadowOfMortality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void canPayPrintedCostAboveStartingLife() {
        harness.setLife(player1, 21);
        harness.castFromHand(player1, new ShadowOfMortality(), "{13}{B}{B}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shadow of Mortality");
    }

    @Test
    void usesCommanderStartingLifeForPartialReduction() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 34);
        harness.setLife(player2, 40);
        harness.castFromHand(player1, new ShadowOfMortality(), "{7}{B}{B}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shadow of Mortality");
    }

    @Test
    void canRemoveAllGenericCostWhileAboveTwentyLifeInCommander() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 27);
        harness.setLife(player2, 40);
        harness.castFromHand(player1, new ShadowOfMortality(), "{B}{B}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shadow of Mortality");
    }
}
