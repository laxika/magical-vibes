package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SummoningTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for no mana after an opponent counters your creature spell")
    void castsForFreeAfterOpponentCountersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        SummoningTrap trap = new SummoningTrap();
        harness.setHand(player1, List.of(bears, trap));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(
                new LlanowarElves(), new MightOfOaks(), new LlanowarElves(), new MightOfOaks(),
                new LlanowarElves(), new MightOfOaks(), new LlanowarElves()));

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3, 4, 5)));
    }

    @Test
    @DisplayName("The alternate cost requires an opponent to have countered a creature spell")
    void alternateCostRequiresOpponentCounteredCreatureSpell() {
        SummoningTrap trap = new SummoningTrap();
        harness.setHand(player1, List.of(trap));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A countered noncreature spell does not enable the alternate cost")
    void noncreatureSpellDoesNotEnableAlternateCost() {
        GrizzlyBears bears = new GrizzlyBears();
        MightOfOaks might = new MightOfOaks();
        SummoningTrap trap = new SummoningTrap();
        harness.addToBattlefield(player1, bears);
        harness.setHand(player1, List.of(might, trap));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
