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
        harness.setHand(player1, List.of(new AmphibiousKavu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new ThornscapeFamiliar());
        harness.setHand(player2, List.of(new CalderaKavu()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
