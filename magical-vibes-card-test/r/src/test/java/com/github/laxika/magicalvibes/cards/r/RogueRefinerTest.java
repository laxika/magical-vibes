package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RogueRefiner.class, Forest.class})
class RogueRefinerTest extends BaseCardTest {

    @Test
    void enteringBattlefieldDrawsACardAndGivesTwoEnergyCounters() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new RogueRefiner(), "{1}{G}{U}");

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawAndEnergyWaitForTheEnterTriggerToResolve() {
        harness.setLibrary(player1, List.of(new RogueRefiner()));
        harness.castFromHand(player1, new RogueRefiner(), "{1}{G}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedEntriesAddEnergyAndDrawOnlyForTheirController() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player2, List.of(new RogueRefiner(), new RogueRefiner()));
        gd.setPlayerEnergyCounters(player2.getId(), 3);
        gd.setPlayerEnergyCounters(player1.getId(), 1);

        harness.castFromHand(player2, new RogueRefiner(), "{1}{G}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(5);

        harness.castFromHand(player2, new RogueRefiner(), "{1}{G}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(7);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }
}
