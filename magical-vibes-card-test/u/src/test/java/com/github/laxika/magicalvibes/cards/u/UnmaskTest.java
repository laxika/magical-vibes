package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Rouse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Unmask.class, Forest.class, Rouse.class})
class UnmaskTest extends BaseCardTest {

    @Test
    void revealsHandAndDiscardsChosenNonlandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Rouse(), new Forest())));
        harness.setHand(player1, List.of(new Unmask()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Rouse");
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName()).containsExactly("Forest");
    }

    @Test
    void canCastByExilingBlackCardFromHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Rouse())));
        harness.setHand(player1, List.of(new Unmask(), new Rouse()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Rouse");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Rouse");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void alternateCostRequiresBlackCard() {
        harness.setHand(player1, List.of(new Unmask(), new Forest()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetYourself() {
        harness.setHand(player1, new ArrayList<>(List.of(new Unmask(), new Rouse())));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Rouse");
    }

    @Test
    void landCreaturesAreExcludedFromNonlandChoices() {
        Card landCreature = new Forest();
        landCreature.setType(CardType.CREATURE);
        landCreature.setAdditionalTypes(Set.of(CardType.LAND));
        harness.setHand(player2, new ArrayList<>(List.of(landCreature, new Rouse())));
        harness.setHand(player1, List.of(new Unmask()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
    }
    @Test
    void emptyHandResolvesWithoutAChoice() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Unmask()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Unmask");
    }

    @Test
    void landOnlyHandResolvesWithoutDiscarding() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Unmask()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Unmask");
    }

    @Test
    void casterChoosesExactlyOneOfMultipleNonlandCards() {
        Rouse chosen = new Rouse();
        Unmask remaining = new Unmask();
        harness.setHand(player2, List.of(new Forest(), chosen, remaining));
        harness.setHand(player1, List.of(new Unmask()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1, 2);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).contains(remaining).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canExileBlackCardBeforeUnmaskInHand() {
        Rouse payment = new Rouse();
        harness.setHand(player1, List.of(payment, new Unmask()));
        harness.setHand(player2, List.of());

        harness.castInstantWithAlternateExileFromHand(player1, 1, player2.getId(), 0);

        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactly(payment);
        harness.assertNotInHand(player1, "Rouse");
        harness.assertNotInGraveyard(player1, "Rouse");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Unmask");
    }

    @Test
    void cannotExileUnmaskItselfToPayItsAlternateCost() {
        harness.setHand(player1, List.of(new Unmask()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, player2.getId(), 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).isEmpty();
    }
}
