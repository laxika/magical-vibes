package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PetraSphinx.class, ChainLightning.class})
class PetraSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts the target player to name a card")
    void resolvingPromptsTargetPlayer() {
        addReadySphinx(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player2.getId());
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.TargetPlayerNameCardRevealTopChoice.class);
    }

    @Test
    @DisplayName("Correct name puts the top card into the target's hand")
    void correctNameGoesToHand() {
        addReadySphinx(player1);

        ChainLightning topCard = new ChainLightning();
        harness.setLibrary(player2, List.of(topCard));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Chain Lightning");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(card -> card.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Wrong name puts the top card into the target's graveyard without dealing damage")
    void wrongNameGoesToGraveyardWithoutDamage() {
        harness.setLife(player2, 20);
        addReadySphinx(player1);

        ChainLightning topCard = new ChainLightning();
        harness.setLibrary(player2, List.of(topCard));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Petra Sphinx");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Empty library does not crash or create a card choice")
    void emptyLibraryHandledGracefully() {
        harness.setLife(player2, 20);
        addReadySphinx(player1);

        harness.setLibrary(player2, List.of());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Petra Sphinx");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may be the target and name a matching card from their library")
    void controllerCanBeTargeted() {
        addReadySphinx(player1);

        ChainLightning topCard = new ChainLightning();
        harness.setLibrary(player1, List.of(topCard));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, "Chain Lightning");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card.getId().equals(topCard.getId()));
    }

    private void addReadySphinx(Player player) {
        addCreatureReady(player, new PetraSphinx());
    }
}
