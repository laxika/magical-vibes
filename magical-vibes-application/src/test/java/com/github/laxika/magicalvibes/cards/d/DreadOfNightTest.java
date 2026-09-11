package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CelestialDawn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.PearlDragon;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadOfNight.class, PearlDragon.class, GrizzlyBears.class, SamiteHealer.class})
class DreadOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("White creature gets -1/-1")
    void debuffsWhiteCreature() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new PearlDragon());

        // 4/4 base -> 3/3
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-white creature is not affected")
    void doesNotDebuffNonWhiteCreature() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // 2/2 green, unaffected
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects controller's own white creatures too")
    void debuffsOwnWhiteCreature() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new PearlDragon());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Dread of Night give -2/-2 to a white creature")
    void twoStack() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new PearlDragon());

        // 4/4 base -> 2/2
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reduces a 1/1 white creature to 0/0")
    void reducesSmallWhiteCreatureToZero() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent healer = harness.addToBattlefieldAndReturn(player2, new SamiteHealer());

        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(0);
    }

    @Test
    @DisplayName("A 1/1 white creature dies to state-based actions")
    void oneToughnessWhiteCreatureDies() {
        harness.addToBattlefield(player1, new DreadOfNight());
        harness.addToBattlefield(player2, new SamiteHealer());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Samite Healer");
    }

    @Test
    @DisplayName("Debuff is removed when Dread of Night leaves the battlefield")
    void debuffRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new DreadOfNight());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new PearlDragon());
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Dread of Night"));

        // Back to base 4/4
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
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
