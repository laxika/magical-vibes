package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LastCaress.class, GaeasSkyfolk.class})
class LastCaressTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses 1 life, controller gains 1 life, and controller draws a card")
    void resolvesAllEffects() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new GaeasSkyfolk()));
        harness.setHand(player1, List.of(new LastCaress()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 17);
        harness.assertInHand(player1, "Gaea's Skyfolk");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target the controller as the player who loses life")
    void canTargetSelf() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new LastCaress()));
        harness.setLibrary(player1, List.of(new GaeasSkyfolk()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 10);
        harness.assertInHand(player1, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent skyfolk = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());

        harness.setHand(player1, List.of(new LastCaress()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, skyfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
