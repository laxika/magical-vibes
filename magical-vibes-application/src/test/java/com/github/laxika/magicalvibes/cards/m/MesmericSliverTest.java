package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MesmericSliver.class, MetallicSliver.class, GrizzlyBears.class})
class MesmericSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver entering may fateseal an opponent's library")
    void sliverEnteringMayFatesealOpponentLibrary() {
        addCreatureReady(player1, new MesmericSliver());
        Card topCard = libraryCard("Opponent top card");
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new MetallicSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.Scry fateseal = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(fateseal.playerId()).isEqualTo(player1.getId());
        assertThat(fateseal.libraryOwnerId()).isEqualTo(player2.getId());
        assertThat(fateseal.causesScryTriggers()).isFalse();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Mesmeric Sliver grants the ability to itself")
    void grantsAbilityToItself() {
        Card topCard = libraryCard("Opponent top card");
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new MesmericSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Declining the ability leaves the opponent's library unchanged")
    void decliningFatesealLeavesLibraryUnchanged() {
        addCreatureReady(player1, new MesmericSliver());
        Card topCard = libraryCard("Opponent top card");
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new MetallicSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Mesmeric Sliver does not grant the ability to non-Slivers")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new MesmericSliver());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Card libraryCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
