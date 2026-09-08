package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodChinRager.class, DromokaWarrior.class, GrizzlyBears.class})
class BloodChinRagerTest extends BaseCardTest {

    @Test
    void attackingGivesMenaceToWarriorsYouControl() {
        Permanent rager = addCreatureReady(player1, new BloodChinRager());
        Permanent warrior = addCreatureReady(player1, new DromokaWarrior());
        Permanent nonWarrior = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingWarrior = addCreatureReady(player2, new DromokaWarrior());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, rager, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonWarrior, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingWarrior, Keyword.MENACE)).isFalse();
    }

    @Test
    void attackingAnotherWarriorDoesNotTriggerBloodChinRager() {
        Permanent rager = addCreatureReady(player1, new BloodChinRager());
        Permanent warrior = addCreatureReady(player1, new DromokaWarrior());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, rager, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.MENACE)).isFalse();
    }

    @Test
    void menaceWearsOffAtEndOfTurn() {
        Permanent rager = addCreatureReady(player1, new BloodChinRager());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, rager, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rager, Keyword.MENACE)).isFalse();
    }
}
