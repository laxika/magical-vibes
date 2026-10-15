package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ConiferStrider;
import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.f.Flatten;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodChinRager.class, DromokaWarrior.class, ConiferStrider.class, Flatten.class})
class BloodChinRagerTest extends BaseCardTest {

    @Test
    void attackingGivesMenaceToWarriorsYouControl() {
        Permanent rager = addCreatureReady(player1, new BloodChinRager());
        Permanent warrior = addCreatureReady(player1, new DromokaWarrior());
        Permanent nonWarrior = addCreatureReady(player1, new ConiferStrider());
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

    @Test
    void warriorsEnteringBeforeResolutionGainMenaceButLaterWarriorsDoNot() {
        Permanent rager = addCreatureReady(player1, new BloodChinRager());
        addCreatureReady(player2, new DromokaWarrior());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, rager, Keyword.MENACE)).isFalse();

        Permanent earlyWarrior = harness.enterBattlefieldAndReturn(player1, new DromokaWarrior());
        resolveAllTriggers();
        Permanent lateWarrior = harness.enterBattlefieldAndReturn(player1, new DromokaWarrior());

        assertThat(gqs.hasKeyword(gd, rager, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, earlyWarrior, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, lateWarrior, Keyword.MENACE)).isFalse();
    }

    @Test
    void attackTriggerStillGrantsMenaceAfterRagerDies() {
        Permanent rager = addCreatureReady(player1, new BloodChinRager());
        Permanent warrior = addCreatureReady(player1, new DromokaWarrior());
        addCreatureReady(player2, new DromokaWarrior());

        declareAttackers(List.of(0));
        harness.setHand(player2, List.of(new Flatten()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, rager.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Blood-Chin Rager");
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.MENACE)).isTrue();
    }

    @Test
    void grantedMenacePreventsBlockingWithOnlyOneCreature() {
        addCreatureReady(player1, new BloodChinRager());
        addCreatureReady(player1, new DromokaWarrior());
        addCreatureReady(player2, new DromokaWarrior());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void grantedMenaceAllowsBlockingWithTwoCreatures() {
        addCreatureReady(player1, new BloodChinRager());
        addCreatureReady(player2, new DromokaWarrior());
        addCreatureReady(player2, new DromokaWarrior());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(2)
                .allSatisfy(blocker -> assertThat(blocker.isBlocking()).isTrue());
    }
}
