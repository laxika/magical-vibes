package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Acridian;
import com.github.laxika.magicalvibes.cards.e.Expunge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Remembrance.class, Acridian.class, Expunge.class})
class RemembranceTest extends BaseCardTest {

    @Test
    @DisplayName("May search for a card with the same name as the nontoken creature that died")
    void searchesForSameNamedCard() {
        harness.addToBattlefield(player1, new Remembrance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Acridian());
        Card found = new Acridian();
        Card unrelated = new Expunge();
        harness.setLibrary(player1, List.of(unrelated, found));

        destroyCreature(player2, creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getId)
                .containsExactly(found.getId());
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Acridian());
        Card found = new Acridian();
        harness.setLibrary(player1, List.of(found));

        destroyCreature(player2, creature);

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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Acridian());
        Card unrelated = new Expunge();
        harness.setLibrary(player1, List.of(unrelated));

        destroyCreature(player2, creature);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(unrelated.getId()));
    }

    @Test
    @DisplayName("Does not trigger when an opponent's nontoken creature dies")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new Remembrance());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Acridian());

        destroyCreature(player1, creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a token creature you control dies")
    void tokenCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new Remembrance());
        Permanent token = addTokenCreature(player1);

        destroyCreature(player2, token);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void destroyCreature(Player destroyer, Permanent creature) {
        harness.forceActivePlayer(destroyer);
        harness.clearPriorityPassed();
        harness.setHand(destroyer, List.of(new Expunge()));
        harness.addMana(destroyer, ManaColor.BLACK, 3);
        harness.castInstant(destroyer, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addTokenCreature(Player player) {
        Card tokenCard = new Card();
        tokenCard.setName("Soldier");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setColor(CardColor.WHITE);
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);
        return harness.addToBattlefieldAndReturn(player, tokenCard);
    }
}
