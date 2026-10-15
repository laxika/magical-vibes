package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseOfTheFilchedFalcon.class, Candlestick.class, GrizzlyBears.class})
class CaseOfTheFilchedFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when it enters the battlefield")
    void investigatesWhenItEnters() {
        castCase();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Solves when you control three artifacts")
    void solvesWithThreeArtifacts() {
        Permanent casePermanent = castCase();
        harness.addToBattlefield(player1, new Candlestick());
        harness.addToBattlefield(player1, new Candlestick());

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("Does not solve when you control fewer than three artifacts")
    void doesNotSolveWithFewerThanThreeArtifacts() {
        Permanent casePermanent = castCase();
        harness.addToBattlefield(player1, new Candlestick());

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("The solved ability turns a noncreature artifact into a Bird creature")
    void solvedAbilityAnimatesTargetArtifact() {
        Permanent casePermanent = castCase();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Candlestick());
        harness.addToBattlefield(player1, new Candlestick());
        resolveEndStepTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(casePermanent.isSolved()).isTrue();
        assertThat(findPermanents(player1, "Case of the Filched Falcon")).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature with the solved ability")
    void cannotTargetCreature() {
        castCase();
        harness.addToBattlefield(player1, new Candlestick());
        harness.addToBattlefield(player1, new Candlestick());
        resolveEndStepTriggers();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact");
    }

    @Test
    @DisplayName("The solved ability cannot be activated before solving")
    void cannotActivateWhileUnsolved() {
        castCase();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Candlestick());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("solved");
        harness.assertOnBattlefield(player1, "Case of the Filched Falcon");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent artifacts do not count toward solving")
    void opponentArtifactsDoNotCount() {
        Permanent casePermanent = castCase();
        harness.addToBattlefield(player2, new Candlestick());
        harness.addToBattlefield(player2, new Candlestick());

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("The Case does not solve during an opponent's end step")
    void doesNotSolveDuringOpponentsEndStep() {
        Permanent casePermanent = castCase();
        harness.addToBattlefield(player1, new Candlestick());
        harness.addToBattlefield(player1, new Candlestick());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(casePermanent.isSolved()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The solve condition is checked again when the trigger resolves")
    void doesNotSolveIfArtifactIsSacrificedInResponse() {
        Permanent casePermanent = castCase();
        harness.addToBattlefield(player1, new Candlestick());
        harness.addToBattlefield(player1, new Candlestick());
        harness.setLibrary(player1, List.of(new Candlestick()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("A solved Case can animate an opponent's artifact permanently")
    void animatesOpponentsArtifactAndPersistsAfterCleanup() {
        castCase();
        harness.addToBattlefield(player1, new Candlestick());
        harness.addToBattlefield(player1, new Candlestick());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Candlestick());
        resolveEndStepTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Case of the Filched Falcon");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(findPermanent(player2, "Candlestick")).isSameAs(target);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.CLUE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.EQUIPMENT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.BIRD)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("The solved ability has no effect if its target leaves before resolution")
    void targetCanBeSacrificedInResponse() {
        castCase();
        harness.addToBattlefield(player1, new Candlestick());
        harness.addToBattlefield(player1, new Candlestick());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Candlestick());
        harness.setLibrary(player2, List.of(new Candlestick(), new Candlestick(), new Candlestick()));
        resolveEndStepTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
                harness.passBothPriorities();
            }
        });
        harness.assertInGraveyard(player1, "Case of the Filched Falcon");
        harness.assertInGraveyard(player2, "Candlestick");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Solving is retained after falling below three artifacts")
    void remainsSolvedAfterLosingArtifacts() {
        Permanent casePermanent = castCase();
        harness.addToBattlefield(player1, new Candlestick());
        harness.addToBattlefield(player1, new Candlestick());
        harness.setLibrary(player1, List.of(new Candlestick()));
        resolveEndStepTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(casePermanent.isSolved()).isTrue();
        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, clue.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, clue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, clue)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, clue)).isEqualTo(4);
    }

    @Test
    @DisplayName("The animated artifact retains its activated abilities")
    void animatedArtifactCanStillBeSacrificedToDraw() {
        castCase();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Candlestick());
        harness.addToBattlefield(player1, new Candlestick());
        harness.setLibrary(player1, List.of(new Candlestick()));
        resolveEndStepTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Candlestick");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    private Permanent castCase() {
        harness.setHand(player1, List.of(new CaseOfTheFilchedFalcon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Case of the Filched Falcon");
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
