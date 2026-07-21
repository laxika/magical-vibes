package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeerOfTheLastTomorrowTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills three cards; the Seer is tapped and a card is discarded")
    void millsThreeCards() {
        Permanent seer = addReadySeer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 6) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();
        List<Card> topThree = List.of(deck.get(0), deck.get(1), deck.get(2));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(topThree);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can target yourself with the mill ability")
    void canTargetSelf() {
        addReadySeer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 6) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadySeer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new SeerOfTheLastTomorrow());
        seer.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    private Permanent addReadySeer(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SeerOfTheLastTomorrow());
        perm.setSummoningSick(false);
        return perm;
    }
}
