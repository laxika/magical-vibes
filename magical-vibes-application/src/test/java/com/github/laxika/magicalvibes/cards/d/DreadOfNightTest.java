package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArmoredPegasus;
import com.github.laxika.magicalvibes.cards.c.CelestialDawn;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.SkyshroudCondor;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoredPegasus.class, CelestialDawn.class, DreadOfNight.class, Opalescence.class, SkyshroudCondor.class, SoltariFootSoldier.class})
class DreadOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("White creature gets -1/-1")
    void debuffsWhiteCreature() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent pegasus = harness.addToBattlefieldAndReturn(player2, new ArmoredPegasus());

        // 1/2 base -> 0/1
        assertThat(gqs.getEffectivePower(gd, pegasus)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, pegasus)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-white creature is not affected")
    void doesNotDebuffNonWhiteCreature() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent condor = harness.addToBattlefieldAndReturn(player2, new SkyshroudCondor());

        // 2/2 blue, unaffected
        assertThat(gqs.getEffectivePower(gd, condor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, condor)).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects controller's own white creatures too")
    void debuffsOwnWhiteCreature() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent pegasus = harness.addToBattlefieldAndReturn(player1, new ArmoredPegasus());

        assertThat(gqs.getEffectivePower(gd, pegasus)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, pegasus)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Dread of Night give -2/-2 to a white creature")
    void twoStack() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent pegasus = harness.addToBattlefieldAndReturn(player2, new ArmoredPegasus());

        // 1/2 base -> -1/0
        assertThat(gqs.getEffectivePower(gd, pegasus)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, pegasus)).isEqualTo(0);
    }

    @Test
    @DisplayName("Reduces a 1/1 white creature to 0/0")
    void reducesSmallWhiteCreatureToZero() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent soldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(0);
    }

    @Test
    @DisplayName("A white creature reduced to 0 toughness dies to state-based actions")
    void zeroToughnessWhiteCreatureDies() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player2, new SoltariFootSoldier());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Soltari Foot Soldier");
        harness.assertInGraveyard(player2, "Soltari Foot Soldier");
    }

    @Test
    @DisplayName("Debuff is removed when Dread of Night leaves the battlefield")
    void debuffRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent pegasus = harness.addToBattlefieldAndReturn(player2, new ArmoredPegasus());
        assertThat(gqs.getEffectivePower(gd, pegasus)).isEqualTo(0);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Dread of Night"));

        // Back to base 1/2
        assertThat(gqs.getEffectivePower(gd, pegasus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pegasus)).isEqualTo(2);
    }

    @Test
    @CardUsed({CelestialDawn.class, Opalescence.class})
    @DisplayName("A white Dread of Night that becomes a creature affects itself")
    void affectsItselfWhenItBecomesWhiteCreature() {
        Permanent dread = harness.addToBattlefieldAndReturn(player1, new DreadOfNight());
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, dread)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dread)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, dread)).isEqualTo(0);
    }
}
