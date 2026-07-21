package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazakethTheFoulbloodedTest extends BaseCardTest {

    // ===== Activated ability: pay 2 life, sacrifice another creature, tutor a card =====

    @Test
    @DisplayName("Pays 2 life, sacrifices another creature, and searches a card into hand")
    void abilityTutorsCardToHand() {
        harness.setLife(player1, 20);
        Permanent razaketh = addCreatureReady(player1, new RazakethTheFoulblooded());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setDeck(player1, List.of(new Forest()));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(razaketh);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities(); // resolve ability → library search prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Paid 2 life
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        // Other creature was sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Tutored card is in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Cannot activate when Razaketh is the only creature (can't sacrifice itself)")
    void cannotSacrificeItself() {
        harness.setLife(player1, 20);
        Permanent razaketh = addCreatureReady(player1, new RazakethTheFoulblooded());
        setDeck(player1, List.of(new Forest()));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(razaketh);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with less than 2 life")
    void cannotActivateWithInsufficientLife() {
        harness.setLife(player1, 1);
        Permanent razaketh = addCreatureReady(player1, new RazakethTheFoulblooded());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setDeck(player1, List.of(new Forest()));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(razaketh);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
