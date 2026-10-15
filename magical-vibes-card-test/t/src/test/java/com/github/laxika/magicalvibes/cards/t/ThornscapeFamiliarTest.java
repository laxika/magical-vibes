package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.a.AmphibiousKavu;
import com.github.laxika.magicalvibes.cards.c.CalderaKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornscapeFamiliar.class, CalderaKavu.class, AncientSpider.class, AmphibiousKavu.class})
class ThornscapeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Red spells you cast cost {1} less")
    void redSpellsCostOneLess() {
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        harness.castFromHand(player1, new CalderaKavu(), "{1}{R}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Caldera Kavu"));
    }

    @Test
    @DisplayName("White spells you cast cost {1} less")
    void whiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        harness.castFromHand(player1, new AncientSpider(), "{1}{G}{W}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Ancient Spider"));
    }

    @Test
    @DisplayName("Spells of other colors are not reduced")
    void otherColorsAreNotReduced() {
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        assertThatThrownBy(() -> harness.castFromHand(player1, new AmphibiousKavu(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        assertThatThrownBy(() -> harness.castFromHand(player2, new CalderaKavu(), "{1}{R}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Familiars each reduce the generic cost")
    void multipleFamiliarsStack() {
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        harness.addToBattlefield(player1, new ThornscapeFamiliar());

        harness.castFromHand(player1, new CalderaKavu(), "{R}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Caldera Kavu"));
    }

    @Test
    @DisplayName("Excess reduction stops at zero generic mana")
    void excessReductionStopsAtZeroGenericMana() {
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        harness.addToBattlefield(player1, new ThornscapeFamiliar());

        harness.castFromHand(player1, new AncientSpider(), "{G}{W}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Ancient Spider"));
    }

    @Test
    @DisplayName("Excess reduction cannot pay a colored mana requirement")
    void excessReductionDoesNotReduceColoredMana() {
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        harness.addToBattlefield(player1, new ThornscapeFamiliar());

        assertThatThrownBy(() -> harness.castFromHand(player1, new AncientSpider(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Familiar in hand does not reduce spell costs")
    void familiarInHandDoesNotReduceCosts() {
        harness.setHand(player1, List.of(new CalderaKavu(), new ThornscapeFamiliar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
