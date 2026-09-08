package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

        Card topCard = createNamedCard("Lightning Bolt");
        gd.playerDecks.get(player2.getId()).addFirst(topCard);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lightning Bolt");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(card -> card.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Wrong name puts the top card into the target's graveyard without dealing damage")
    void wrongNameGoesToGraveyardWithoutDamage() {
        harness.setLife(player2, 20);
        addReadySphinx(player1);

        Card topCard = createNamedCard("Grizzly Bears");
        gd.playerDecks.get(player2.getId()).addFirst(topCard);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lightning Bolt");

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

        gd.playerDecks.get(player2.getId()).clear();
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lightning Bolt");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadySphinx(Player player) {
        PetraSphinx card = new PetraSphinx();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private static Card createNamedCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        return card;
    }
}
