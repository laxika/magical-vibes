package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThoughtDistortionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles noncreature, nonland cards from the target's hand and graveyard")
    void exilesMatchingCardsFromHandAndGraveyard() {
        Card handSpell = new Shock();
        Card handCreature = new GrizzlyBears();
        Card handLand = new Island();
        Card graveyardSpell = new Peek();
        Card graveyardCreature = new GrizzlyBears();
        Card graveyardLand = new Island();

        harness.setHand(player2, List.of(handSpell, handCreature, handLand));
        harness.setGraveyard(player2, List.of(graveyardSpell, graveyardCreature, graveyardLand));
        harness.setHand(player1, List.of(new ThoughtDistortion()));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCreature, handLand);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(graveyardCreature, graveyardLand);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(handSpell, graveyardSpell);
    }

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCountered() {
        ThoughtDistortion distortion = new ThoughtDistortion();
        harness.setHand(player1, List.of(distortion));
        harness.setHand(player2, List.of(new Cancel(), new GrizzlyBears()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, distortion.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thought Distortion");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .contains("Cancel");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ThoughtDistortion()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
