package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RobeOfMirrors;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSuperHeroCivilWar.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class,
        RobeOfMirrors.class, WordOfSeizing.class})
class TheSuperHeroCivilWarTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I gains control of up to two creatures with total mana value six or less")
    void chapterIGainsControlWithinManaValueLimit() {
        Permanent saga = addSaga(0);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        triggerChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bear.getId(), giant.getId(), wurm.getId());
        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bear.getId())
                .doesNotContain(wurm.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga, bear, giant);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear, giant);
    }

    @Test
    @DisplayName("Chapter II boosts your creatures and grants vigilance until end of turn")
    void chapterIIBoostsAndGrantsVigilanceUntilEndOfTurn() {
        addSaga(1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Chapter III has a controlled creature fight another creature")
    void chapterIIIFightsAnotherCreature() {
        Permanent saga = addSaga(2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opposingCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opposingCreature.getId())
                .doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void chapterICanChooseNoTargets() {
        addSaga(0);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    void chapterICanChooseOneCreatureAtTheManaValueLimit() {
        addSaga(0);
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.addToBattlefield(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
    }

    @Test
    void chapterIControlPersistsWhenSagaChangesController() {
        Permanent saga = addSaga(0);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);

        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, saga.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(saga);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    @Test
    void chapterIStillResolvesWhenSagaChangesControllerInResponse() {
        Permanent saga = addSaga(0);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, saga.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(saga);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
    }

    @Test
    void chapterIControlEndsWhenSagaLeavesTheBattlefield() {
        Permanent saga = addSaga(0);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, saga));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    void chapterIIDoesNotAffectCreaturesEnteringAfterResolution() {
        addSaga(1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        triggerChapter();
        harness.passBothPriorities();
        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void chapterIIICanDeclineTheSecondTarget() {
        Permanent saga = addSaga(2);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, giant.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void chapterIIICanFightAnotherCreatureYouControl() {
        addSaga(2);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, giant.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    void chapterIIIDoesNotFightIfFirstTargetChangesController() {
        addSaga(2);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, giant.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(giant, bear);
        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(bear.getMarkedDamage()).isZero();
    }

    @Test
    void chapterICannotChooseACreatureWithShroud() {
        Permanent shroudedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RobeOfMirrors()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, shroudedBear.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, shroudedBear, Keyword.SHROUD)).isTrue();
        Permanent legalBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSaga(0);

        triggerChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(legalBear.getId())
                .doesNotContain(shroudedBear.getId());
    }

    @Test
    void chapterIIICannotChooseACreatureWithShroudAsEitherTarget() {
        Permanent shroudedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RobeOfMirrors()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, shroudedBear.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, shroudedBear, Keyword.SHROUD)).isTrue();
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent legalBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSaga(2);

        triggerChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(giant.getId())
                .doesNotContain(shroudedBear.getId());
        harness.handlePermanentChosen(player1, giant.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(legalBear.getId())
                .doesNotContain(shroudedBear.getId());
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheSuperHeroCivilWar());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
