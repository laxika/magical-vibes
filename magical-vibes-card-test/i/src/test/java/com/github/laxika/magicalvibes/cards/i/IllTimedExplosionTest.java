package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllTimedExplosion.class, GrizzlyBears.class, WindDrake.class, HillGiant.class})
class IllTimedExplosionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, optionally discards two, and damages each creature by the greatest discarded mana value")
    void damagesByGreatestDiscardedManaValue() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new WindDrake()));
        harness.castFromHand(player1, new IllTimedExplosion(), "{2}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);

        int windDrakeIndex = gd.playerHands.get(player1.getId()).indexOf(
                gd.playerHands.get(player1.getId()).stream()
                        .filter(card -> card instanceof WindDrake)
                        .findFirst().orElseThrow());
        harness.handleCardChosen(player1, windDrakeIndex);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the discard still keeps the drawn cards and deals no damage")
    void decliningDiscardDealsNoDamage() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new WindDrake()));
        harness.castFromHand(player1, new IllTimedExplosion(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Discarding creates a separate damage trigger that waits for priority passes")
    void damageWaitsForReflexiveTriggerResolution() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new WindDrake()));
        harness.castFromHand(player1, new IllTimedExplosion(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Ill-Timed Explosion");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The greatest discarded value is used, rather than the sum, and both players' creatures are damaged")
    void damagesBothBattlefieldsWithoutAddingManaValues() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, new IllTimedExplosion(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
