package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeldonsCaneTest extends BaseCardTest {

    // ===== Activation =====

    @Test
    @DisplayName("Activating exiles Feldon's Cane as cost and puts ability on stack")
    void activatingExilesSelfAndPutsOnStack() {
        addReadyCane(player1);

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
        Permanent cane = addReadyCane(player1);
        cane.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving shuffles controller's graveyard into their library")
    void resolvingShufflesGraveyardIntoLibrary() {
        addReadyCane(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GiantSpider()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 3);
    }

    @Test
    @DisplayName("Only shuffles the controller's own graveyard, not the opponent's")
    void doesNotShuffleOpponentGraveyard() {
        addReadyCane(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GiantSpider()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("A dead token in the graveyard is left behind, not shuffled into the library")
    void deadTokenIsNotShuffledIntoLibrary() {
        addReadyCane(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token));
        harness.clearPriorityPassed();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Only the Grizzly Bears card travels; the token ceases to exist (CR 111.7).
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Zombie Token"));
    }

    // ===== Helpers =====

    private static Card tokenCreature() {
        Card card = new Card();
        card.setName("Zombie Token");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setToken(true);
        return card;
    }

    private Permanent addReadyCane(Player player) {
        Permanent perm = new Permanent(new FeldonsCane());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
