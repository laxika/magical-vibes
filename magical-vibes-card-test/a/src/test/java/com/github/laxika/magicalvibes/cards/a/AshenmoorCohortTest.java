package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BoggartRamGang;
import com.github.laxika.magicalvibes.cards.w.WoundReflection;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshenmoorCohort.class, AshenmoorGouger.class, BoggartRamGang.class, WoundReflection.class})
class AshenmoorCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/3 when no other black creature is controlled")
    void noBoostWhenAlone() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());
        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost with a non-black creature")
    void noBoostWithNonBlackCreature() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());
        harness.addToBattlefield(player1, new BoggartRamGang());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost with a black noncreature permanent")
    void noBoostWithBlackNoncreaturePermanent() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());
        harness.addToBattlefield(player1, new WoundReflection());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 when controller controls another black creature")
    void boostWithAnotherBlackCreature() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());
        harness.addToBattlefield(player1, new AshenmoorCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped multicolored black creature grants the boost")
    void boostWithTappedMulticoloredBlackCreature() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());
        Permanent gouger = harness.addToBattlefieldAndReturn(player1, new AshenmoorGouger());
        gouger.tap();

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, gouger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gouger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple other black creatures grant only one +1/+1 bonus")
    void multipleBlackCreaturesDoNotStackBoost() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());
        harness.addToBattlefield(player1, new AshenmoorCohort());
        harness.addToBattlefield(player1, new AshenmoorGouger());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's black creature does not grant the boost")
    void opponentBlackCreatureDoesNotCount() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());
        harness.addToBattlefield(player2, new AshenmoorCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses boost when the other black creature leaves the battlefield")
    void losesBoostWhenBlackCreatureLeaves() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());
        Permanent otherCohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(otherCohort);

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("Black creatures outside the battlefield do not count; entering grants the boost immediately")
    void gainsBoostOnlyWhenBlackCreatureEntersBattlefield() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new AshenmoorCohort());
        harness.setHand(player1, List.of(new AshenmoorGouger()));
        harness.setGraveyard(player1, List.of(new AshenmoorCohort()));

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);

        harness.enterBattlefieldAndReturn(player1, new AshenmoorGouger());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
