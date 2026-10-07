package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Stranglehold;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptWithDiscovery.class, Forest.class, CommandTower.class, CosisTrickster.class, Stranglehold.class})
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

    @Test
    @DisplayName("An opponent who fails to find still earns a reward search")
    void failedOpponentSearchStillCreatesReward() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        castTemptWithDiscovery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty opponent library still earns a reward search")
    void emptyOpponentLibraryStillCreatesReward() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of());

        castTemptWithDiscovery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Failing the initial search does not skip the offer or reward")
    void failedInitialSearchDoesNotSkipRemainingInstructions() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        castTemptWithDiscovery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Nonbasic lands can be found and enter untapped")
    void findsNonbasicLand() {
        harness.setLibrary(player1, List.of(new CommandTower()));
        harness.setLibrary(player2, List.of(new Forest()));

        castTemptWithDiscovery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Command Tower");
        assertThat(findPermanent(player1, "Command Tower").isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller shuffles only once, after all searches")
    void defersControllerShuffleUntilAfterRewardSearch() {
        harness.addToBattlefield(player2, new CosisTrickster());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        castTemptWithDiscovery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.stack).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(CosisTrickster.class);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A prevented opponent search neither shuffles nor earns a reward")
    void preventedOpponentSearchDoesNotShuffle() {
        harness.addToBattlefield(player1, new Stranglehold());
        harness.addToBattlefield(player1, new CosisTrickster());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        castTemptWithDiscovery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castTemptWithDiscovery() {
        harness.castFromHand(player1, new TemptWithDiscovery(), "{3}{G}");
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
