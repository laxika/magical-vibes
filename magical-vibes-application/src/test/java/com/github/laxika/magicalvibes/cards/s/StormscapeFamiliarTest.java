package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MarshCrocodile;
import com.github.laxika.magicalvibes.cards.n.NightscapeFamiliar;
import com.github.laxika.magicalvibes.cards.t.ThornscapeFamiliar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormscapeFamiliar.class, SunscapeFamiliar.class, NightscapeFamiliar.class,
        ThornscapeFamiliar.class, SilverDrake.class, MarshCrocodile.class})
class StormscapeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("White spells you cast cost {1} less")
    void whiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.castFromHand(player1, new SunscapeFamiliar(), "{W}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Sunscape Familiar"));
    }

    @Test
    @DisplayName("Black spells you cast cost {1} less")
    void blackSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.castFromHand(player1, new NightscapeFamiliar(), "{B}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Nightscape Familiar"));
    }

    @Test
    @DisplayName("Multicolored white spells cost {1} less")
    void multicoloredWhiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.castFromHand(player1, new SilverDrake(), "{W}{U}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Silver Drake"));
    }

    @Test
    @DisplayName("Spells of other colors are not reduced")
    void otherColorsAreNotReduced() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());

        assertThatThrownBy(() -> harness.castFromHand(player1, new ThornscapeFamiliar(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());

        assertThatThrownBy(() -> harness.castFromHand(player2, new NightscapeFamiliar(), "{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not pay a spell's colored mana requirement")
    void reductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());

        assertThatThrownBy(() -> harness.castFromHand(player1, new SunscapeFamiliar(), "{1}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Familiars stack their generic mana reductions")
    void multipleFamiliarsReduceCostByTwo() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.castFromHand(player1, new MarshCrocodile(), "{U}{B}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Marsh Crocodile"));
    }

    @Test
    @DisplayName("Excess reductions do not pay colored mana")
    void excessReductionsDoNotPayColoredMana() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.addToBattlefield(player1, new StormscapeFamiliar());

        assertThatThrownBy(() -> harness.castFromHand(player1, new SilverDrake(), "{W}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blue spells are not reduced")
    void blueSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());

        assertThatThrownBy(() -> harness.castFromHand(player1, new StormscapeFamiliar(), "{U}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Familiar in hand does not reduce spell costs")
    void familiarInHandDoesNotReduceCosts() {
        harness.setHand(player1, List.of(new SunscapeFamiliar(), new StormscapeFamiliar()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
