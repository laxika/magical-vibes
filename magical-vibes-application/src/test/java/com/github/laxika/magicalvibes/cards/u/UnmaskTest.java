package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnmaskTest extends BaseCardTest {

    @Test
    void revealsHandAndDiscardsChosenNonlandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new Unmask()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName()).containsExactly("Forest");
    }

    @Test
    void canCastByExilingBlackCardFromHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.setHand(player1, List.of(new Unmask(), new Duress()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Duress");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void alternateCostRequiresBlackCard() {
        harness.setHand(player1, List.of(new Unmask(), new Forest()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }
}
