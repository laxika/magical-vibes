package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.d.DarklingStalker;
import com.github.laxika.magicalvibes.cards.s.SeleniaDarkAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JetMedallion.class, DarklingStalker.class, CanopySpider.class, SeleniaDarkAngel.class})
class JetMedallionTest extends BaseCardTest {

    @Test
    @DisplayName("Black spells you cast cost {1} less")
    void blackSpellsCostOneLess() {
        harness.addToBattlefield(player1, new JetMedallion());
        // Darkling Stalker costs {3}{B} — with the {1} reduction it should cost {2}{B}
        harness.setHand(player1, List.of(new DarklingStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Darkling Stalker"));
    }

    @Test
    @DisplayName("Black multicolored spells you cast cost {1} less")
    void multicoloredBlackSpellsCostOneLess() {
        harness.addToBattlefield(player1, new JetMedallion());
        // Selenia costs {3}{W}{B}; its black color qualifies despite being multicolored.
        harness.setHand(player1, List.of(new SeleniaDarkAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Selenia, Dark Angel"));
    }

    @Test
    @DisplayName("Non-black spells are not reduced")
    void nonBlackSpellsNotReduced() {
        harness.addToBattlefield(player1, new JetMedallion());
        // Canopy Spider costs {1}{G} — not black, so only {G} is not enough
        harness.setHand(player1, List.of(new CanopySpider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new JetMedallion());
        harness.setHand(player2, List.of(new DarklingStalker()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
