package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeldonsCane.class, Squire.class, TormodsCrypt.class})
class FeldonsCaneTest extends BaseCardTest {
    @Test
    @DisplayName("Activating exiles Feldon's Cane as cost and puts ability on stack")
    void activatingExilesSelfAndPutsOnStack() {
        harness.addToBattlefieldAndReturn(player1, new FeldonsCane());

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Feldon's Cane");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Feldon's Cane"));
        harness.assertNotInGraveyard(player1, "Feldon's Cane");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent cane = harness.addToBattlefieldAndReturn(player1, new FeldonsCane());
        cane.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Resolving shuffles controller's graveyard into their library")
    void resolvingShufflesGraveyardIntoLibrary() {
        harness.addToBattlefieldAndReturn(player1, new FeldonsCane());
        harness.setGraveyard(player1, List.of(new Squire(), new Squire(), new TormodsCrypt()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 3);
    }

    @Test
    @DisplayName("Only shuffles the controller's own graveyard, not the opponent's")
    void doesNotShuffleOpponentGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new FeldonsCane());
        harness.setGraveyard(player1, List.of(new Squire()));
        harness.setGraveyard(player2, List.of(new TormodsCrypt()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Tormod's Crypt");
    }

    @Test
    @DisplayName("A dead token ceases to exist instead of being shuffled into the library")
    void deadTokenIsNotShuffledIntoLibrary() {
        harness.addToBattlefieldAndReturn(player1, new FeldonsCane());
        harness.setGraveyard(player1, List.of(new Squire()));
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token));
        harness.clearPriorityPassed();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Only the Squire card travels; the token ceases to exist (CR 111.7).
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Zombie Token"));
    }
    private static Card tokenCreature() {
        Card card = new Card();
        card.setName("Zombie Token");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setToken(true);
        return card;
    }

    @Test
    @DisplayName("An empty graveyard does not prevent activation or resolution")
    void resolvesWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new FeldonsCane());
        harness.setGraveyard(player1, List.of());
        Squire libraryCard = new Squire();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof FeldonsCane);
    }

    @Test
    @DisplayName("Cards entering the graveyard after activation are shuffled on resolution")
    void usesGraveyardAtResolution() {
        harness.addToBattlefield(player1, new FeldonsCane());
        Squire originalCard = new Squire();
        TormodsCrypt laterCard = new TormodsCrypt();
        Squire libraryCard = new Squire();
        harness.setGraveyard(player1, List.of(originalCard));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(originalCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.setGraveyard(player1, List.of(originalCard, laterCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryCard, originalCard, laterCard);
    }

    @Test
    @DisplayName("Exiling the graveyard in response leaves the Cane exiled and resolves its ability")
    void graveyardExiledInResponse() {
        harness.addToBattlefield(player1, new FeldonsCane());
        harness.addToBattlefield(player2, new TormodsCrypt());
        Squire graveyardCard = new Squire();
        Squire libraryCard = new Squire();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof FeldonsCane);
        harness.assertInGraveyard(player2, "Tormod's Crypt");
    }

}
