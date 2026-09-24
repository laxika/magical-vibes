package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.h.HigureTheStillWind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NezumiShadowWatcher.class, HigureTheStillWind.class, GnarledMass.class})
class NezumiShadowWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself to destroy target Ninja")
    void destroysTargetNinja() {
        harness.addToBattlefield(player1, new NezumiShadowWatcher());
        harness.addToBattlefield(player2, new HigureTheStillWind());
        UUID target = harness.getPermanentId(player2, "Higure, the Still Wind");

        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Higure, the Still Wind");
        harness.assertNotOnBattlefield(player1, "Nezumi Shadow-Watcher");
        harness.assertInGraveyard(player1, "Nezumi Shadow-Watcher");
    }

    @Test
    @DisplayName("Cannot target a creature that is not a Ninja")
    void cannotTargetNonNinja() {
        harness.addToBattlefield(player1, new NezumiShadowWatcher());
        harness.addToBattlefield(player2, new GnarledMass());
        UUID target = harness.getPermanentId(player2, "Gnarled Mass");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Nezumi Shadow-Watcher");
    }

    @Test
    @DisplayName("Can target and destroy a Ninja controlled by its controller")
    void destroysOwnNinja() {
        harness.addToBattlefield(player1, new NezumiShadowWatcher());
        harness.addToBattlefield(player1, new HigureTheStillWind());
        UUID target = harness.getPermanentId(player1, "Higure, the Still Wind");

        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Higure, the Still Wind");
        harness.assertInGraveyard(player1, "Nezumi Shadow-Watcher");
    }
}
