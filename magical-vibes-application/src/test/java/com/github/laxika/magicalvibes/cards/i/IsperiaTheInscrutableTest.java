package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IsperiaTheInscrutable.class, AirElemental.class, GrizzlyBears.class})
class IsperiaTheInscrutableTest extends BaseCardTest {

    @Test
    @DisplayName("A matching name reveals the hand and searches for a creature with flying")
    void matchingNameSearchesForFlyingCreature() {
        Card flyingCreature = new AirElemental();
        Card nonFlyingCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(flyingCreature, nonFlyingCreature));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        dealCombatDamageWithIsperia();

        harness.handleListChoice(player1, "Grizzly Bears");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(flyingCreature);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(flyingCreature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(findCardInHand(player2, "Grizzly Bears"));
    }

    @Test
    @DisplayName("A name absent from the revealed hand does not search")
    void absentNameDoesNotSearch() {
        Card flyingCreature = new AirElemental();
        harness.setLibrary(player1, List.of(flyingCreature));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        dealCombatDamageWithIsperia();

        harness.handleListChoice(player1, "Air Elemental");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(flyingCreature);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void dealCombatDamageWithIsperia() {
        addCreatureReady(player1, new IsperiaTheInscrutable());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(ChoiceContext.ChooseCardNameRevealHandThenChoice.class);
    }

    private Card findCardInHand(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        return gd.playerHands.get(player.getId()).stream()
                .filter(card -> card.getName().equals(cardName))
                .findFirst()
                .orElseThrow();
    }
}
