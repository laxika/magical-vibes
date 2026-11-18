package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TowerDefense.class, DiscipleOfTheOldWays.class})
class TowerDefenseTest extends BaseCardTest {

    @Test
    @DisplayName("Tower Defense gives own creatures +0/+5 and reach")
    void buffsOwnCreaturesOnly() {
        Permanent p1a = addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent p1b = addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent p2 = addCreatureReady(player2, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new TowerDefense()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, p1a)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, p1a)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, p1b)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, p1a, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, p1b, Keyword.REACH)).isTrue();

        assertThat(gqs.getEffectivePower(gd, p2)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, p2)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, p2, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Tower Defense effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new TowerDefense()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotLaterArrivals() {
        harness.setHand(player1, List.of(new TowerDefense()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0);
        Permanent beforeResolution = addCreatureReady(player1, new DiscipleOfTheOldWays());

        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new DiscipleOfTheOldWays());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.REACH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.REACH)).isFalse();
    }

    @Test
    void repeatedCastsStackAndBothExpire() {
        Permanent creature = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new TowerDefense(), new TowerDefense()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }
}
