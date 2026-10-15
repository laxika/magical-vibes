package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorrowedKnowledge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivergentEquation.class, Shock.class, Opt.class, GrizzlyBears.class, BorrowedKnowledge.class})
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

    @Test
    void returnsBothInstantAndSorceryCardsButOnlyFromYourGraveyard() {
        Card instant = new DivergentEquation();
        Card sorcery = new BorrowedKnowledge();
        Card opposingSorcery = new BorrowedKnowledge();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setGraveyard(player2, List.of(opposingSorcery));
        harness.setHand(player1, List.of(new DivergentEquation()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, 2, null);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divergent Equation");
        harness.assertInHand(player1, "Borrowed Knowledge");
        harness.assertInGraveyard(player2, "Borrowed Knowledge");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Divergent Equation"));
    }

    @Test
    void positiveXAllowsChoosingNoTargets() {
        Card sorcery = new BorrowedKnowledge();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new DivergentEquation()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, 2, null);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Borrowed Knowledge");
        harness.assertNotInHand(player1, "Borrowed Knowledge");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Divergent Equation"));
    }

    @Test
    void positiveXWithEmptyGraveyardStillResolvesAndExilesItself() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new DivergentEquation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Divergent Equation");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Divergent Equation"));
    }

    @Test
    void returnsRemainingLegalTargetAndExilesItself() {
        Card first = new BorrowedKnowledge();
        Card second = new BorrowedKnowledge();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DivergentEquation()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, 2, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(first);
        gd.addToExile(player1.getId(), first);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(second.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Divergent Equation"));
    }

    @Test
    void allTargetsLeavingGraveyardPreventsResolutionAndSelfExile() {
        Card sorcery = new BorrowedKnowledge();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new DivergentEquation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(sorcery);
        gd.addToExile(player1.getId(), sorcery);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divergent Equation");
        harness.assertNotInHand(player1, "Borrowed Knowledge");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getName().equals("Divergent Equation"));
    }
}
