package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MysidianElder;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
        MysidianElder.class, Shock.class})
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

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperRamuh());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private Permanent findSaga() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SummonEsperRamuh)
                .findFirst()
                .orElseThrow();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
