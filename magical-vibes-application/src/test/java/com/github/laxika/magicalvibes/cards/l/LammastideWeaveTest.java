package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LammastideWeaveTest extends BaseCardTest {

    private void cast(com.github.laxika.magicalvibes.model.Player targetPlayer) {
        harness.setHand(player1, List.of(new LammastideWeave()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, targetPlayer.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving prompts the controller to name a card")
    void promptsControllerToNameCard() {
        cast(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.NameCardMillGainLifeChoice.class);
    }

    @Test
    @DisplayName("Milling the named card gains life equal to its mana value and draws a card")
    void matchGainsLifeAndDraws() {
        Card top = createNamedCard("Weave Target", "{2}{U}"); // mana value 3
        gd.playerDecks.get(player2.getId()).addFirst(top);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player2);
        harness.handleListChoice(player1, "Weave Target");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Milling a card that doesn't match the name still mills and draws but gains no life")
    void mismatchMillsAndDrawsWithoutLife() {
        Card top = createNamedCard("Weave Target", "{2}{U}");
        gd.playerDecks.get(player2.getId()).addFirst(top);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player2);
        harness.handleListChoice(player1, "Something Else");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target yourself: mills your own card and gains life on a match")
    void canTargetSelf() {
        Card top = createNamedCard("Self Mill", "{1}{G}"); // mana value 2
        gd.playerDecks.get(player1.getId()).addFirst(top);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player1);
        harness.handleListChoice(player1, "Self Mill");

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Interaction clears and spell leaves the stack after resolving")
    void interactionClearsAfterResolve() {
        Card top = createNamedCard("Weave Target", "{2}{U}");
        gd.playerDecks.get(player2.getId()).addFirst(top);

        cast(player2);
        harness.handleListChoice(player1, "Weave Target");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private static Card createNamedCard(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(CardColor.BLUE);
        return card;
    }
}
