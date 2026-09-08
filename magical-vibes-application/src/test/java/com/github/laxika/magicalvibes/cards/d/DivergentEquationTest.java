package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DivergentEquationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns any number up to X of your instant and sorcery cards and exiles itself")
    void returnsUpToXInstantAndSorceryCards() {
        Card shock = new Shock();
        Card opt = new Opt();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, opt, bears));
        harness.setHand(player1, List.of(new DivergentEquation()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        gs.playCard(gd, player1, 0, 2, null, null);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(shock.getId(), opt.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Divergent Equation"));
    }

    @Test
    @DisplayName("X=0 returns no cards and still exiles the spell")
    void xZeroReturnsNothing() {
        Card opt = new Opt();
        harness.setGraveyard(player1, List.of(opt));
        harness.setHand(player1, List.of(new DivergentEquation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Divergent Equation"));
    }
}
