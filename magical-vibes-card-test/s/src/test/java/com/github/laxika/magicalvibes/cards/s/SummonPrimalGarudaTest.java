package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonPrimalGaruda.class, GrizzlyBears.class})
class SummonPrimalGarudaTest extends BaseCardTest {

    @Test
    void chapterIDamagesOnlyTappedCreatureAnOpponentControls() {
        addSagaWithLore(0);
        Permanent tappedOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedOpponentCreature.tap();
        Permanent untappedOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(tappedOpponentCreature.getId());

        harness.handlePermanentChosen(player1, tappedOpponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(untappedOpponentCreature)
                .doesNotContain(tappedOpponentCreature);
    }

    @Test
    void chapterIIBoostsAndGrantsFlyingToAnotherCreatureYouControl() {
        addSagaWithLore(1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ownCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void chapterIIIBoostsAnotherCreatureAndThenSacrificesTheSaga() {
        Permanent saga = addSagaWithLore(2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ownCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void chapterRequiresATargetWhenALegalCreatureExists(int previousLore) {
        addSagaWithLore(previousLore);
        Permanent creature = harness.addToBattlefieldAndReturn(
                previousLore == 0 ? player2 : player1, new GrizzlyBears());
        creature.tap();

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(creature.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
    }

    @Test
    void castingTheSagaTriggersChapterIAsItEnters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        harness.castFromHand(player1, new SummonPrimalGaruda(), "{3}{W}");
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "Summon: Primal Garuda");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIDoesNotDamageATargetThatUntapsBeforeResolution() {
        addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        triggerNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonPrimalGaruda());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
