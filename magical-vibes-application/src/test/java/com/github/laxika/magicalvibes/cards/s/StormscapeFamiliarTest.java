package com.github.laxika.magicalvibes.cards.s;

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
        ThornscapeFamiliar.class, SilverDrake.class})
class StormscapeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("White spells you cast cost {1} less")
    void whiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.setHand(player1, List.of(new SunscapeFamiliar()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Sunscape Familiar"));
    }

    @Test
    @DisplayName("Black spells you cast cost {1} less")
    void blackSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.setHand(player1, List.of(new NightscapeFamiliar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Nightscape Familiar"));
    }

    @Test
    @DisplayName("Multicolored white spells cost {1} less")
    void multicoloredWhiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.setHand(player1, List.of(new SilverDrake()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Silver Drake"));
    }

    @Test
    @DisplayName("Spells of other colors are not reduced")
    void otherColorsAreNotReduced() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.setHand(player1, List.of(new ThornscapeFamiliar()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.setHand(player2, List.of(new NightscapeFamiliar()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
