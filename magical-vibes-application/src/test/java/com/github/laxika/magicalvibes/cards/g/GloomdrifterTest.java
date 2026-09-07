package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gloomdrifter.class, DrudgeSkeletons.class, GrizzlyBears.class, HillGiant.class})
class GloomdrifterTest extends BaseCardTest {

    @Test
    @DisplayName("With threshold, its ETB trigger gives nonblack creatures -2/-2")
    void thresholdTriggersOnEntering() {
        addCreatures();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castGloomdrifter();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        Permanent hillGiant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Hill Giant"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, hillGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hillGiant)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Gloomdrifter"));
    }

    @Test
    @DisplayName("Without threshold, its ETB trigger does not occur")
    void doesNotTriggerBelowThreshold() {
        addCreatures();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castGloomdrifter();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        Permanent hillGiant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Hill Giant"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, hillGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hillGiant)).isEqualTo(3);
    }

    private void addCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player2, new HillGiant());
    }

    private void castGloomdrifter() {
        harness.setHand(player1, List.of(new Gloomdrifter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
