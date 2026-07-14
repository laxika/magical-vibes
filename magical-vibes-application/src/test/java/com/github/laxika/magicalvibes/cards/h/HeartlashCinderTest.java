package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeartlashCinderTest extends BaseCardTest {

    private Permanent castCinder(Player player) {
        harness.setHand(player, List.of(new HeartlashCinder()));
        harness.addMana(player, ManaColor.RED, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities(); // resolve the Cinder, queue ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Heartlash Cinder"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("ETB boost equals the red mana symbols among your permanents (self included)")
    void etbBoostsByRedSymbols() {
        // Hill Giant {3}{R} = 1 red symbol; Cinder itself {1}{R} = 1 red symbol. Total X = 2.
        addCreatureReady(player1, new HillGiant());

        Permanent cinder = castCinder(player1);

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(3); // 1 base + 2
        assertThat(gqs.getEffectiveToughness(gd, cinder)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only red mana symbols count; non-red permanents contribute nothing")
    void etbCountsOnlyRedSymbols() {
        // Grizzly Bears {1}{G} = 0 red symbols; only the Cinder's own {1}{R} counts. X = 1.
        addCreatureReady(player1, new GrizzlyBears());

        Permanent cinder = castCinder(player1);

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2); // 1 base + 1
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostWearsOff() {
        Permanent cinder = castCinder(player1); // X = 1 (self only)
        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cinder)).isEqualTo(1);
    }
}
