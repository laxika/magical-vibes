package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BuriedOgre.class)
class BuriedOgreTest extends BaseCardTest {

    @Test
    void acceptingPregameChoicePutsItInGraveyardAndLosesLife() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        BuriedOgre buriedOgre = new BuriedOgre();
        openingHarness.setHand(openingPlayer, List.of(buriedOgre));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        openingHarness.handleMayAbilityChosen(openingPlayer, true);

        assertThat(openingHarness.getGameData().playerGraveyards.get(openingPlayer.getId()))
                .contains(buriedOgre);
        assertThat(openingHarness.getGameData().playerHands.get(openingPlayer.getId()))
                .doesNotContain(buriedOgre);
        assertThat(openingHarness.getGameData().getLife(openingPlayer.getId())).isEqualTo(19);
        assertThat(openingHarness.getGameData().status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void decliningPregameChoiceLeavesItInHandWithoutLifeLoss() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        BuriedOgre buriedOgre = new BuriedOgre();
        openingHarness.setHand(openingPlayer, List.of(buriedOgre));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingPlayer, false);

        assertThat(openingHarness.getGameData().playerGraveyards.get(openingPlayer.getId()))
                .doesNotContain(buriedOgre);
        assertThat(openingHarness.getGameData().playerHands.get(openingPlayer.getId()))
                .contains(buriedOgre);
        assertThat(openingHarness.getGameData().getLife(openingPlayer.getId())).isEqualTo(20);
        assertThat(openingHarness.getGameData().status).isEqualTo(GameStatus.RUNNING);
    }
}
