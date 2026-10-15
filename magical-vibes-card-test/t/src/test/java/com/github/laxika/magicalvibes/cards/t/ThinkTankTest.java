package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThinkTank.class, AngelicWall.class})
class ThinkTankTest extends BaseCardTest {

    @Test
    @DisplayName("Surveil puts the top card into the graveyard when accepted")
    void surveilAccepted() {
        addThinkTank(player1);
        Card topCard = new AngelicWall();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        harness.assertInGraveyard(player1, "Angelic Wall");
    }

    @Test
    @DisplayName("Surveil leaves the top card on the library when declined")
    void surveilDeclined() {
        addThinkTank(player1);
        Card topCard = new AngelicWall();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        addThinkTank(player1);
        Card topCard = new AngelicWall();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Surveil 1 does not prompt when the library is empty")
    void surveilWithEmptyLibrary() {
        addThinkTank(player1);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
    }

    @Test
    @DisplayName("Each Think Tank resolves its own surveil choice")
    void multipleCopiesSurveilSeparately() {
        addThinkTank(player1);
        addThinkTank(player1);
        Card first = new AngelicWall();
        Card second = new AngelicWall();
        harness.setLibrary(player1, List.of(first, second, new AngelicWall()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The upkeep ability resolves after Think Tank leaves the battlefield")
    void triggerResolvesWithoutSource() {
        addThinkTank(player1);
        Card topCard = new AngelicWall();
        harness.setLibrary(player1, List.of(topCard, new AngelicWall()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second player's Think Tank surveils that player's library")
    void secondPlayerSurveilsOwnLibrary() {
        addThinkTank(player2);
        Card topCard = new AngelicWall();
        Card opponentTopCard = new AngelicWall();
        harness.setLibrary(player2, List.of(topCard, new AngelicWall()));
        harness.setLibrary(player1, List.of(opponentTopCard, new AngelicWall()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(opponentTopCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard, opponentTopCard);
    }
    private void addThinkTank(Player player) {
        harness.addToBattlefield(player, new ThinkTank());
    }
}
