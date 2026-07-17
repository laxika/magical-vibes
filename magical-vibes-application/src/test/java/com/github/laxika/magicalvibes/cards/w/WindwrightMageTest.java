package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FlightSpellbomb;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindwrightMageTest extends BaseCardTest {

    @Test
    @DisplayName("No flying when graveyard is empty")
    void noFlyingWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No flying with only a non-artifact card in graveyard")
    void noFlyingWithNonArtifactCard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying with an artifact card in graveyard")
    void hasFlyingWithArtifactCard() {
        harness.setGraveyard(player1, List.of(new FlightSpellbomb()));
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Loses flying when the artifact card leaves the graveyard")
    void losesFlyingWhenArtifactRemoved() {
        harness.setGraveyard(player1, List.of(new FlightSpellbomb()));
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's artifact card in graveyard does not grant flying")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(new FlightSpellbomb()));
        harness.addToBattlefield(player1, new WindwrightMage());

        assertThat(gqs.hasKeyword(gd, findMage(), Keyword.FLYING)).isFalse();
    }

    private Permanent findMage() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Windwright Mage"))
                .findFirst().orElseThrow();
    }
}
