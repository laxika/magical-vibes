package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MysidianElder;
import com.github.laxika.magicalvibes.cards.t.ThePrimaVista;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonEsperRamuh.class, Forest.class, GiantGrowth.class, GrizzlyBears.class,
        MysidianElder.class, Shock.class, ThePrimaVista.class})
class SummonEsperRamuhTest extends BaseCardTest {

    @Test
    void chapterIDealsDamageEqualToNoncreatureNonlandCardsInGraveyard() {
        addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears opponentCard = new GrizzlyBears();
        opponentCard.setToughness(4);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, opponentCard);
        harness.setGraveyard(player1, List.of(
                new Shock(), new GiantGrowth(), new Forest(), new GrizzlyBears()));

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId())
                .doesNotContain(ownCreature.getId(), findSaga().getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void chapterIIBoostsOnlyWizardsUntilEndOfTurn() {
        addSagaWithLore(1);
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new MysidianElder());
        Permanent nonWizard = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MysidianElder());
        int wizardPower = gqs.getEffectivePower(gd, wizard);
        int sagaPower = gqs.getEffectivePower(gd, findSaga());
        int nonWizardPower = gqs.getEffectivePower(gd, nonWizard);
        int opponentPower = gqs.getEffectivePower(gd, opponentCreature);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(wizardPower + 1);
        assertThat(gqs.getEffectivePower(gd, findSaga())).isEqualTo(sagaPower + 1);
        assertThat(gqs.getEffectivePower(gd, nonWizard)).isEqualTo(nonWizardPower);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(opponentPower);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(wizardPower);
        assertThat(gqs.getEffectivePower(gd, findSaga())).isEqualTo(sagaPower);
    }

    @Test
    void chapterIIIBoostsWizardsAndSacrificesTheSaga() {
        Permanent saga = addSagaWithLore(2);
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new MysidianElder());
        int wizardPower = gqs.getEffectivePower(gd, wizard);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(wizardPower + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void chapterITriggersWhenTheSagaEnters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MysidianElder());
        harness.setGraveyard(player1, List.of(new Shock()));

        Permanent saga = harness.enterBattlefieldAndReturn(player1, new SummonEsperRamuh());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void chapterICountsTheControllersGraveyardAtResolution() {
        addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MysidianElder());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new Shock(), new GiantGrowth(), new Shock()));

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of(new Shock(), new ThePrimaVista(),
                new Forest(), new SummonEsperRamuh()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void chapterIDealsNoDamageWithOnlyCreatureAndLandCardsInGraveyard() {
        addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MysidianElder());
        harness.setGraveyard(player1, List.of(new Forest(), new SummonEsperRamuh()));

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void chapterIRequiresAnOpposingCreatureTargetWhenOneIsAvailable() {
        addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MysidianElder());
        harness.setGraveyard(player1, List.of(new Shock()));

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
    }

    @Test
    void chapterIFailsToResolveIfTheTargetIsNowControlledByItsController() {
        addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MysidianElder());
        harness.setGraveyard(player1, List.of(new Shock()));

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void chapterIIOnlyBoostsWizardsPresentWhenItResolves() {
        addSagaWithLore(1);
        advanceToNextChapter();
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new MysidianElder());
        int power = gqs.getEffectivePower(gd, wizard);
        int toughness = gqs.getEffectiveToughness(gd, wizard);

        harness.passBothPriorities();
        Permanent laterWizard = harness.addToBattlefieldAndReturn(player1, new MysidianElder());

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(toughness);
        assertThat(gqs.getEffectivePower(gd, laterWizard)).isEqualTo(power);
    }

    @Test
    void finalChapterWaitsToSacrificeAndItsBoostExpiresAtEndOfTurn() {
        Permanent saga = addSagaWithLore(2);
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new MysidianElder());
        int power = gqs.getEffectivePower(gd, wizard);

        advanceToNextChapter();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(power + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(power);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperRamuh());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private Permanent findSaga() {
        return findPermanent(player1, "Summon: Esper Ramuh");
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
