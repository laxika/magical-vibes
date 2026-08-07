package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmeraldMedallionTest extends BaseCardTest {

    @Test
    @DisplayName("Green spells you cast cost {1} less")
    void greenSpellsCostOneLess() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        // Grizzly Bears costs {1}{G} — with the {1} reduction it should cost just {G}
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Non-green spells are not reduced")
    void nonGreenSpellsNotReduced() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        // Walking Corpse costs {1}{B} — not green, so only {B} is not enough
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
