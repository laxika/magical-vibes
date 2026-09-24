package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GalinasKnight;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShorelineRaider;
import com.github.laxika.magicalvibes.cards.t.TectonicInstability;
import com.github.laxika.magicalvibes.cards.t.TidalVisionary;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HalamDjinn.class, GalinasKnight.class, Island.class, ShorelineRaider.class,
        TectonicInstability.class, TidalVisionary.class})
class HalamDjinnTest extends BaseCardTest {

    private Permanent addHalamDjinn() {
        return harness.addToBattlefieldAndReturn(player1, new HalamDjinn());
    }

    @Test
    @DisplayName("Shrinks when red is the most common color")
    void shrinksWhenRedIsMostCommon() {
        Permanent halam = addHalamDjinn();

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shrinks when red is tied for most common color")
    void shrinksWhenRedIsTied() {
        Permanent halam = addHalamDjinn();
        harness.addToBattlefield(player2, new ShorelineRaider());

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent halam = addHalamDjinn();
        harness.addToBattlefield(player2, new ShorelineRaider());
        harness.addToBattlefield(player2, new ShorelineRaider());

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts each color of multicolored permanents")
    void countsEachColorOfMulticoloredPermanents() {
        Permanent halam = addHalamDjinn();
        harness.addToBattlefield(player2, new GalinasKnight());
        harness.addToBattlefield(player2, new GalinasKnight());

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ignores colorless permanents")
    void doesNotCountColorlessPermanents() {
        Permanent halam = addHalamDjinn();
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts colored noncreature permanents")
    void countsColoredNoncreaturePermanents() {
        Permanent halam = addHalamDjinn();
        harness.addToBattlefield(player2, new TectonicInstability());
        harness.addToBattlefield(player2, new TectonicInstability());

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(3);
    }

    @Test
    @DisplayName("Re-evaluates when a permanent changes color")
    void reevaluatesWhenPermanentChangesColor() {
        Permanent halam = addHalamDjinn();
        addCreatureReady(player1, new TidalVisionary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShorelineRaider());
        harness.addToBattlefield(player2, new ShorelineRaider());

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(5);

        harness.activateAbility(player1, 1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(3);
    }
}
