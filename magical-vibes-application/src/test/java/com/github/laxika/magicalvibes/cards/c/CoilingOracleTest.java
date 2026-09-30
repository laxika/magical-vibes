package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.s.SimicInitiate;
import com.github.laxika.magicalvibes.cards.s.SimicGrowthChamber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoilingOracle.class, GhostQuarter.class, SimicInitiate.class, SimicGrowthChamber.class})
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
        harness.handlePermanentChosen(player1, chamber.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == chamber)
                .anyMatch(permanent -> permanent.getCard() == oracle);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chamber);
    }
}
