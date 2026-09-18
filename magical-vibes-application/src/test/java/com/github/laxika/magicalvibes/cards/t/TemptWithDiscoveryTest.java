package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptWithDiscovery.class, Forest.class})
class TemptWithDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Accepted opponent searches, then the controller searches for the reward land")
    void acceptedOpponentCreatesOneAdditionalControllerSearch() {
        Forest initialLand = new Forest();
        Forest rewardLand = new Forest();
        Forest opponentLand = new Forest();
        harness.setLibrary(player1, List.of(initialLand, rewardLand));
        harness.setLibrary(player2, List.of(opponentLand));

        castTemptWithDiscovery();
        harness.passBothPriorities();

        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the offer prevents both the opponent search and the reward search")
    void decliningOpponentDoesNotCreateAdditionalSearches() {
        Forest initialLand = new Forest();
        Forest opponentLand = new Forest();
        harness.setLibrary(player1, List.of(initialLand));
        harness.setLibrary(player2, List.of(opponentLand));

        castTemptWithDiscovery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castTemptWithDiscovery() {
        harness.castFromHand(player1, new TemptWithDiscovery(), "{3}{G}");
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
