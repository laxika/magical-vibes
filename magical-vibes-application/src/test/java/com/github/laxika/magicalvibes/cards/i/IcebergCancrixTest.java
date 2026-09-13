package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IcebergCancrix.class, SnowCoveredIsland.class, GrizzlyBears.class})
class IcebergCancrixTest extends BaseCardTest {

    @Test
    @DisplayName("May mill two cards when another snow permanent enters under your control")
    void mayMillForControlledSnowPermanent() {
        harness.addToBattlefield(player1, new IcebergCancrix());
        harness.setLibrary(player2, libraryWithFiveCards());

        harness.enterBattlefieldAndReturn(player1, new SnowCoveredIsland());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("May decline the snow-entry mill")
    void mayDeclineMill() {
        harness.addToBattlefield(player1, new IcebergCancrix());
        harness.setLibrary(player2, libraryWithFiveCards());

        harness.enterBattlefieldAndReturn(player1, new SnowCoveredIsland());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for nonsnow permanents or an opponent's snow permanent")
    void ignoresNonsnowAndOpponentSnowEntries() {
        harness.addToBattlefield(player1, new IcebergCancrix());
        harness.setLibrary(player2, libraryWithFiveCards());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.enterBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for Iceberg Cancrix entering itself")
    void ignoresItsOwnEntry() {
        harness.setLibrary(player2, libraryWithFiveCards());

        harness.enterBattlefieldAndReturn(player1, new IcebergCancrix());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()
        );
    }
}
