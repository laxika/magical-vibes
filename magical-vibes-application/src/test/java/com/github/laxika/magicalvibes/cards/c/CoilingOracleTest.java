package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.s.SealOfDoom;
import com.github.laxika.magicalvibes.cards.s.SimicInitiate;
import com.github.laxika.magicalvibes.cards.s.SimicGrowthChamber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoilingOracle.class, GhostQuarter.class, SimicInitiate.class, SimicGrowthChamber.class,
        SealOfDoom.class})
class CoilingOracleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a revealed land onto the battlefield")
    void landEntersBattlefield() {
        Card oracle = new CoilingOracle();
        Card land = new GhostQuarter();
        harness.setLibrary(player1, List.of(land, new SimicInitiate()));

        harness.castFromHand(player1, oracle, "{G}{U}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == oracle)
                .anyMatch(permanent -> permanent.getCard() == land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("ETB puts a revealed nonland card into its controller's hand")
    void nonlandEntersHand() {
        Card oracle = new CoilingOracle();
        Card nonland = new SimicInitiate();
        harness.setLibrary(player1, List.of(nonland, new GhostQuarter()));

        harness.castFromHand(player1, oracle, "{G}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(nonland);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == oracle);
    }

    @Test
    @DisplayName("ETB does nothing when its controller's library is empty")
    void emptyLibraryDoesNothing() {
        Card oracle = new CoilingOracle();
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, oracle, "{G}{U}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == oracle);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB resolves for a land put onto the battlefield")
    void revealedLandEtbResolves() {
        Card oracle = new CoilingOracle();
        Card chamber = new SimicGrowthChamber();
        harness.setLibrary(player1, List.of(chamber));

        harness.castFromHand(player1, oracle, "{G}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, findPermanent(player1, chamber.getName()).getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == chamber)
                .anyMatch(permanent -> permanent.getCard() == oracle);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chamber);
    }

    @Test
    @DisplayName("ETB uses the top card when the trigger resolves")
    void usesLibraryAtResolution() {
        Card oracle = new CoilingOracle();
        Card originalTop = new GhostQuarter();
        Card newTop = new SimicInitiate();
        harness.setLibrary(player1, List.of(originalTop));

        harness.castFromHand(player1, oracle, "{G}{U}");
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.setLibrary(player1, List.of(newTop, originalTop));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(newTop);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
    }

    @Test
    @DisplayName("ETB still resolves after Coiling Oracle is destroyed")
    void triggerResolvesAfterSourceLeaves() {
        Card oracle = new CoilingOracle();
        Card nonland = new SimicInitiate();
        harness.addToBattlefield(player2, new SealOfDoom());
        harness.setLibrary(player1, List.of(nonland));

        harness.castFromHand(player1, oracle, "{G}{U}");
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, findPermanent(player1, oracle.getName()).getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, oracle.getName());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast uses the entering controller's library")
    void enteringWithoutCastingUsesControllersLibrary() {
        harness.setHand(player2, List.of());
        Card oracle = new CoilingOracle();
        Card land = new GhostQuarter();
        Card opponentTop = new SimicInitiate();
        harness.setLibrary(player1, List.of(opponentTop));
        harness.setLibrary(player2, List.of(land));

        harness.enterBattlefieldAndReturn(player2, oracle);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentTop);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A revealed land obeys its enters-tapped ability")
    void revealedLandEntersTapped() {
        Card oracle = new CoilingOracle();
        Card chamber = new SimicGrowthChamber();
        Card otherLand = new GhostQuarter();
        harness.addToBattlefield(player1, otherLand);
        harness.setLibrary(player1, List.of(chamber));

        harness.castFromHand(player1, oracle, "{G}{U}");
        resolveAllTriggers();
        assertThat(findPermanent(player1, chamber.getName()).isTapped()).isTrue();
        harness.handlePermanentChosen(player1, findPermanent(player1, otherLand.getName()).getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, chamber.getName()).isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherLand);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
