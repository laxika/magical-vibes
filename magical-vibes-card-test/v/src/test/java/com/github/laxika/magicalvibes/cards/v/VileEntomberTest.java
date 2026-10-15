package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VileEntomber.class, GrizzlyBears.class, Island.class, Plains.class})
class VileEntomberTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers any library card for the graveyard")
    void searchesAnyLibraryCardIntoGraveyard() {
        Card creature = new GrizzlyBears();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(creature, island, plains));
        castVileEntomber();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(creature, island, plains);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, plains);
    }

    @Test
    @DisplayName("An unrestricted search cannot fail to find in a nonempty library")
    void cannotDeclineSearchWithCardsRemaining() {
        Card card = new VileEntomber();
        harness.setLibrary(player1, List.of(card));
        castVileEntomber();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not leave an unresolved search prompt")
    void emptyLibraryCompletesTrigger() {
        harness.setLibrary(player1, List.of());
        castVileEntomber();

        harness.assertOnBattlefield(player1, "Vile Entomber");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The search can put a land into the controller's graveyard without touching the opponent's library")
    void searchesLandIntoOwnGraveyard() {
        Card land = new Island();
        Card opponentCard = new VileEntomber();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(opponentCard));
        castVileEntomber();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker whose toughness exceeds the damage dealt")
    void deathtouchDestroysLargerBlocker() {
        Permanent attacker = addCreatureReady(player1, new VileEntomber());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new VileEntomber());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
    }

    private void castVileEntomber() {
        harness.castFromHand(player1, new VileEntomber(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
