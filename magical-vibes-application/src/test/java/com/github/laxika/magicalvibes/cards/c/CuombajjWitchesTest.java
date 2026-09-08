package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CuombajjWitchesTest extends BaseCardTest {

    @Test
    void opponentChoosesTheSecondAnyTarget() {
        addCreatureReady(player1, new CuombajjWitches());

        harness.activateAbility(player1, 0, null, player1.getId());

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gameData.getLife(player1.getId())).isEqualTo(19);
        assertThat(gameData.getLife(player2.getId())).isEqualTo(19);
    }
}
