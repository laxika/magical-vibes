package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaggerfangDuo.class})
class DaggerfangDuoTest extends BaseCardTest {

    @Test
    void mayMillTwoCardsWhenItEnters() {
        harness.setLibrary(player1, List.of(new DaggerfangDuo(), new DaggerfangDuo(), new DaggerfangDuo()));
        harness.castFromHand(player1, new DaggerfangDuo(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void mayDeclineToMill() {
        harness.setLibrary(player1, List.of(new DaggerfangDuo(), new DaggerfangDuo()));
        harness.castFromHand(player1, new DaggerfangDuo(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void millsOnlyTheRemainingCardFromAShortLibrary() {
        DaggerfangDuo remaining = new DaggerfangDuo();
        DaggerfangDuo opponentsCard = new DaggerfangDuo();
        harness.setLibrary(player1, List.of(remaining));
        harness.setLibrary(player2, List.of(opponentsCard));
        harness.castFromHand(player1, new DaggerfangDuo(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Daggerfang Duo");
    }

    @Test
    void mayMillFromAnEmptyLibraryWithoutLosing() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new DaggerfangDuo(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertOnBattlefield(player1, "Daggerfang Duo");
    }
}
