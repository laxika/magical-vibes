package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.c.Choke;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmeraldMedallion.class, CanopySpider.class, WindDrake.class, Earthcraft.class, Choke.class})
class EmeraldMedallionTest extends BaseCardTest {

    @Test
    @DisplayName("Green spells you cast cost {1} less")
    void greenSpellsCostOneLess() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        // Canopy Spider costs {1}{G} — with the {1} reduction it should cost just {G}
        harness.castFromHand(player1, new CanopySpider(), "{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-green spells are not reduced")
    void nonGreenSpellsNotReduced() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        // Wind Drake costs {2}{U}; two blue mana cannot pay its unreduced cost.
        assertThatThrownBy(() -> harness.castFromHand(player1, new WindDrake(), "{U}{U}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        assertThatThrownBy(() -> harness.castFromHand(player2, new CanopySpider(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Green noncreature spells you cast cost {1} less")
    void greenNoncreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        harness.castFromHand(player1, new Earthcraft(), "{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Medallions reduce the generic cost cumulatively")
    void multipleMedallionsStack() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        harness.addToBattlefield(player1, new EmeraldMedallion());

        harness.castFromHand(player1, new Choke(), "{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Excess reductions do not remove colored mana requirements")
    void excessReductionDoesNotPayGreenMana() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        harness.addToBattlefield(player1, new EmeraldMedallion());

        assertThatThrownBy(() -> harness.castFromHand(player1, new CanopySpider(), "{1}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Excess reductions still allow casting for the colored cost")
    void excessReductionDoesNotPreventCasting() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        harness.addToBattlefield(player1, new EmeraldMedallion());

        harness.castFromHand(player1, new CanopySpider(), "{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Medallion still reduces spell costs")
    void tappedMedallionStillReducesCosts() {
        harness.addToBattlefieldAndReturn(player1, new EmeraldMedallion()).tap();

        harness.castFromHand(player1, new CanopySpider(), "{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A Medallion in the graveyard does not reduce spell costs")
    void medallionInGraveyardDoesNotReduceCosts() {
        harness.setGraveyard(player1, List.of(new EmeraldMedallion()));

        assertThatThrownBy(() -> harness.castFromHand(player1, new CanopySpider(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
