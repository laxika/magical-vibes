package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KwainItinerantMeddler.class, Forest.class})
class KwainItinerantMeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("Each player who draws gains 1 life")
    void eachPlayerWhoDrawsGainsLife() {
        addReadyKwain(player1);
        setUpLibraries();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Declining the draw does not grant life")
    void decliningDrawDoesNotGrantLife() {
        addReadyKwain(player1);
        setUpLibraries();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
    }

    private Permanent addReadyKwain(Player player) {
        return addCreatureReady(player, new KwainItinerantMeddler());
    }

    private void setUpLibraries() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
    }
}
