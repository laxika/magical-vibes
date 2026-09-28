package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GalinasKnight;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.cards.t.TectonicInstability;
import com.github.laxika.magicalvibes.cards.t.TidalVisionary;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SulamDjinn.class, KavuAggressor.class, GalinasKnight.class, TectonicInstability.class,
        Island.class, TidalVisionary.class})
class SulamDjinnTest extends BaseCardTest {

    private Permanent addSulamDjinn() {
        return harness.addToBattlefieldAndReturn(player1, new SulamDjinn());
    }

    @Test
    @DisplayName("Shrinks when green is the most common color")
    void shrinksWhenGreenIsMostCommon() {
        Permanent sulam = addSulamDjinn();

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Shrinks when green is tied for most common color")
    void shrinksWhenGreenIsTied() {
        Permanent sulam = addSulamDjinn();
        harness.addToBattlefield(player2, new KavuAggressor());

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent sulam = addSulamDjinn();
        harness.addToBattlefield(player2, new KavuAggressor());
        harness.addToBattlefield(player2, new KavuAggressor());

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts each color of multicolored permanents")
    void countsEachColorOfMulticoloredPermanents() {
        Permanent sulam = addSulamDjinn();
        harness.addToBattlefield(player2, new GalinasKnight());
        harness.addToBattlefield(player2, new GalinasKnight());

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts colored noncreature permanents")
    void countsColoredNoncreaturePermanents() {
        Permanent sulam = addSulamDjinn();
        harness.addToBattlefield(player2, new TectonicInstability());
        harness.addToBattlefield(player2, new TectonicInstability());

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not count colorless permanents")
    void doesNotCountColorlessPermanents() {
        Permanent sulam = addSulamDjinn();
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Re-evaluates when a permanent changes color")
    void reevaluatesWhenPermanentChangesColor() {
        Permanent sulam = addSulamDjinn();
        addCreatureReady(player1, new TidalVisionary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KavuAggressor());
        harness.addToBattlefield(player2, new KavuAggressor());

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(6);

        harness.activateAbility(player1, 1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(4);
    }
}
