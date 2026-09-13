package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.z.Zephid;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Crosswinds.class, CradleGuard.class, Zephid.class})
class CrosswindsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with flying get -2/-0")
    void debuffsCreaturesWithFlying() {
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new Zephid());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures without flying are unaffected")
    void doesNotDebuffCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CradleGuard());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The effect applies to flying creatures controlled by either player")
    void affectsOwnFlyingCreature() {
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new Zephid());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Crosswinds effects stack")
    void effectsStack() {
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new Zephid());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Stacked effects can reduce flying creatures to negative power")
    void effectsCanReducePowerBelowZero() {
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player1, new Crosswinds());
        harness.addToBattlefield(player1, new Crosswinds());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new Zephid());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(-3);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }
}
