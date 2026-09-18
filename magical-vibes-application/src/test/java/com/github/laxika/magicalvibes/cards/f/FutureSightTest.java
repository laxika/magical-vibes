package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FutureSight.class, Forest.class, GrizzlyBears.class, Shock.class})
class FutureSightTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may play a land from the top of their library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new FutureSight());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("The controller may cast a spell from the top of their library")
    void castsSpellFromLibraryTop() {
        harness.addToBattlefield(player1, new FutureSight());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("The controller may cast an instant from the top of their library")
    void castsInstantFromLibraryTop() {
        harness.addToBattlefield(player1, new FutureSight());
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Future Sight does not grant its controller access to another player's library")
    void onlyControllerMayUseFutureSight() {
        harness.addToBattlefield(player1, new FutureSight());
        Shock shock = new Shock();
        harness.setLibrary(player2, List.of(shock));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Reveals the controller's top library card to both players")
    void revealsControllerTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Shock"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Shock"));
    }

    @Test
    @DisplayName("Losing all abilities removes Future Sight's land-play permission")
    void losingAllAbilitiesRemovesLandPlayPermission() {
        Permanent futureSight = harness.addToBattlefieldAndReturn(player1, new FutureSight());
        futureSight.setLosesAllAbilitiesUntilEndOfTurn(true);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Losing all abilities removes Future Sight's spell-casting permission")
    void losingAllAbilitiesRemovesSpellCastingPermission() {
        Permanent futureSight = harness.addToBattlefieldAndReturn(player1, new FutureSight());
        futureSight.setLosesAllAbilitiesUntilEndOfTurn(true);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Losing all abilities stops Future Sight from revealing the top card")
    void losingAllAbilitiesStopsTopCardReveal() {
        Permanent futureSight = harness.addToBattlefieldAndReturn(player1, new FutureSight());
        futureSight.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setLibrary(player1, List.of(new Shock()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Shock"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Shock"));
    }
}
