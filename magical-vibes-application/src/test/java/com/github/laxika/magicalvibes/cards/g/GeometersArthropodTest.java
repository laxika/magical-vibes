package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeometersArthropodTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an X spell looks at X cards and puts one into hand")
    void castingXSpellLooksAtPaidXCards() {
        harness.addToBattlefield(player1, new GeometersArthropod());
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new Shock();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new Hurricane()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(first, second, third);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(fourth, second, third);
    }

    @Test
    @DisplayName("Casting a spell without X does not trigger the ability")
    void castingNonXSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GeometersArthropod());
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }
}
