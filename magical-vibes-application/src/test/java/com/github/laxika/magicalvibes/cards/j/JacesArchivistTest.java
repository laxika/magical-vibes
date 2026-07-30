package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacesArchivistTest extends BaseCardTest {

    @Test
    @DisplayName("Every player draws equal to the largest hand discarded")
    void everyoneDrawsGreatestDiscarded() {
        Permanent archivist = addReadyArchivist();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Plains(), new Plains(), new Plains()));

        activate(archivist);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Discarded cards are not redrawn - draws come off the library")
    void discardedCardsGoToGraveyardNotBackToHand() {
        Permanent archivist = addReadyArchivist();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of());

        activate(archivist);

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> gd.playerGraveyards.get(player1.getId()).contains(card));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Nobody draws when every hand is empty")
    void noDrawsWhenAllHandsEmpty() {
        Permanent archivist = addReadyArchivist();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        activate(archivist);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyArchivist() {
        return addCreatureReady(player1, new JacesArchivist());
    }

    private void activate(Permanent archivist) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
