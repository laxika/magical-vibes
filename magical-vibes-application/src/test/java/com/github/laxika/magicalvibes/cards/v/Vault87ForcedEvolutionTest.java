package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MutantSurveyor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vault87ForcedEvolution.class, GrizzlyBears.class, HillGiant.class, Forest.class,
        MutantSurveyor.class})
class Vault87ForcedEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I gains control of a non-Mutant creature")
    void chapterIGainsControlOfNonMutantCreature() {
        addSaga(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mutant = harness.addToBattlefieldAndReturn(player2, new MutantSurveyor());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(mutant.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mutant);
    }

    @Test
    @DisplayName("Chapter II adds a counter and the Mutant subtype to a creature you control")
    void chapterIIBoostsAndMutatesYourCreature() {
        addSaga(1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("Chapter III draws cards equal to the greatest Mutant power")
    void chapterIIIDrawsGreatestMutantPower() {
        addSaga(2);
        Permanent mutant = harness.addToBattlefieldAndReturn(player1, new MutantSurveyor());
        int expectedDraws = gqs.getEffectivePower(gd, mutant);
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + expectedDraws);
    }

    @Test
    @DisplayName("Chapter I cannot target a Mutant creature")
    void chapterICannotTargetMutantCreature() {
        addSaga(0);
        Permanent mutant = harness.addToBattlefieldAndReturn(player2, new MutantSurveyor());

        advanceToNextChapter();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, mutant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chapter III draws nothing when only the opponent controls a Mutant")
    void chapterIIIDrawsNothingWithoutYourOwnMutant() {
        addSaga(2);
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new MutantSurveyor());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.assertInGraveyard(player1, "Vault 87: Forced Evolution");
    }

    @Test
    @DisplayName("Chapter II cannot target an opponent's creature")
    void chapterIICannotTargetOpponentsCreature() {
        addSaga(1);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(own.getId()).doesNotContain(opposing.getId());
        harness.handlePermanentChosen(player1, own.getId());
        harness.passBothPriorities();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposing)).doesNotContain(CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("The stolen creature contributes to chapter III before returning with its mutation")
    void stolenCreatureReturnsAfterFinalChapterWithCounterAndSubtype() {
        addSaga(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.BEAR, CardSubtype.MUTANT);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Vault 87: Forced Evolution");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.BEAR, CardSubtype.MUTANT);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault87ForcedEvolution());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
