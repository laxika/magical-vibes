package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Boompile.class, GrizzlyBears.class, Forest.class})
class BoompileTest extends BaseCardTest {
    @Test
    @DisplayName("Activating Boompile taps it before the coin is flipped")
    void activationPaysTapCostBeforeResolution() {
        harness.addToBattlefield(player1, new Boompile());

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanent(player1, "Boompile").isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gameLogContains("wins the coin flip for Boompile")).isFalse();
        assertThat(gameLogContains("loses the coin flip for Boompile")).isFalse();

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Boompile cannot activate its ability")
    void tappedBoompileCannotActivate() {
        harness.addToBattlefield(player1, new Boompile());
        findPermanent(player1, "Boompile").setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Boompile");
    }

    @Test
    @DisplayName("A winning flip destroys all nonland permanents while lands survive")
    void destroysNonlandsOnlyWhenFlipIsWon() {
        harness.addToBattlefield(player1, new Boompile());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Boompile");
        boolean lost = gameLogContains("loses the coin flip for Boompile");
        assertThat(won ^ lost).isTrue();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        if (won) {
            harness.assertNotOnBattlefield(player1, "Boompile");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        } else {
            harness.assertOnBattlefield(player1, "Boompile");
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Grizzly Bears");
        }
    }
}
