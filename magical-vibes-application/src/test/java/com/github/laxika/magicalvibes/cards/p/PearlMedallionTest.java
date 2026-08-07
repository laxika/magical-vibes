package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PearlMedallionTest extends BaseCardTest {

    @Test
    @DisplayName("White spells you cast cost {1} less")
    void whiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new PearlMedallion());
        // Angel of Mercy costs {4}{W} — with the {1} reduction it should cost {3}{W}
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Angel of Mercy"));
    }

    @Test
    @DisplayName("Non-white spells are not reduced")
    void nonWhiteSpellsNotReduced() {
        harness.addToBattlefield(player1, new PearlMedallion());
        // Grizzly Bears costs {1}{G} — not white, so a single {G} is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new PearlMedallion());
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
