package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArmoredPegasus;
import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.h.HannasCustody;
import com.github.laxika.magicalvibes.cards.s.SeleniaDarkAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PearlMedallion.class, ArmoredPegasus.class, CanopySpider.class,
        HannasCustody.class, SeleniaDarkAngel.class})
class PearlMedallionTest extends BaseCardTest {

    @Test
    @DisplayName("White spells you cast cost {1} less")
    void whiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new PearlMedallion());
        // Armored Pegasus costs {1}{W}; with the {1} reduction it should cost just {W}
        harness.setHand(player1, List.of(new ArmoredPegasus()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-white spells are not reduced")
    void nonWhiteSpellsNotReduced() {
        harness.addToBattlefield(player1, new PearlMedallion());
        // Canopy Spider costs {1}{G} — not white, so a single {G} is not enough
        harness.setHand(player1, List.of(new CanopySpider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new PearlMedallion());
        harness.setHand(player2, List.of(new ArmoredPegasus()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multicolored spells containing white are reduced")
    void multicoloredWhiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new PearlMedallion());
        // Selenia costs {3}{W}{B}; white is one of its colors, so it costs {2}{W}{B}
        harness.setHand(player1, List.of(new SeleniaDarkAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("White noncreature spells are reduced")
    void whiteNoncreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new PearlMedallion());
        // Hanna's Custody costs {2}{W}; with the {1} reduction it should cost {1}{W}
        harness.setHand(player1, List.of(new HannasCustody()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
