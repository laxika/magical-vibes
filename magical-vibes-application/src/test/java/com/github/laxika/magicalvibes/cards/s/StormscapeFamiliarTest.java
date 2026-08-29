package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormscapeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("White spells you cast cost {1} less")
    void whiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Youthful Knight"));
    }

    @Test
    @DisplayName("Black spells you cast cost {1} less")
    void blackSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Spells of other colors are not reduced")
    void otherColorsAreNotReduced() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StormscapeFamiliar());
        harness.setHand(player2, List.of(new WalkingCorpse()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
