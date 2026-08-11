package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RemembranceTest extends BaseCardTest {

    @Test
    @DisplayName("May search for a card with the same name as the nontoken creature that died")
    void searchesForSameNamedCard() {
        harness.addToBattlefield(player1, new Remembrance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card found = new GrizzlyBears();
        Card unrelated = new Forest();
        setLibrary(player1, List.of(unrelated, found));

        destroyCreature(creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getId)
                .containsExactly(found.getId());
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(found.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(unrelated.getId()))
                .noneMatch(card -> card.getId().equals(found.getId()));
    }

    @Test
    @DisplayName("May decline the same-name search")
    void mayDeclineSearch() {
        harness.addToBattlefield(player1, new Remembrance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card found = new GrizzlyBears();
        setLibrary(player1, List.of(found));

        destroyCreature(creature);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(found.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(found.getId()));
    }

    @Test
    @DisplayName("Search may fail to find a same-named card")
    void searchMayFailToFind() {
        harness.addToBattlefield(player1, new Remembrance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card unrelated = new Forest();
        setLibrary(player1, List.of(unrelated));

        destroyCreature(creature);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(unrelated.getId()));
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
