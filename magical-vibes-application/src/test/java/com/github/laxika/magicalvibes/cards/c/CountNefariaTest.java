package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CountNefaria.class, Atog.class, Spellbook.class, TerramorphicExpanse.class})
class CountNefariaTest extends BaseCardTest {

    @Test
    @CardUsed({CountNefaria.class})
    void castsForFullCostWithoutSacrifice() {
        harness.setHand(player1, List.of(new CountNefaria()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Count Nefaria");
    }

    @Test
    @CardUsed({CountNefaria.class, TerramorphicExpanse.class})
    void sacrificingMultipleLandsReducesCostOnlyOnce() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new CountNefaria()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Count Nefaria");
    }

    @Test
    @CardUsed({CountNefaria.class, TerramorphicExpanse.class})
    void opponentsSacrificeDoesNotReduceCost() {
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new TerramorphicExpanse());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new CountNefaria()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast Count Nefaria for {3}{B} after sacrificing a permanent")
    void castsWithCostReductionAfterSacrifice() {
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        CountNefaria countNefaria = new CountNefaria();
        harness.setHand(player1, List.of(countNefaria));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast Count Nefaria for {3}{B} without sacrificing a permanent")
    void cannotCastWithoutSacrifice() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new CountNefaria()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The cost reduction does not reduce Count Nefaria's colored mana cost")
    void reductionDoesNotApplyToColoredMana() {
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new CountNefaria()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
