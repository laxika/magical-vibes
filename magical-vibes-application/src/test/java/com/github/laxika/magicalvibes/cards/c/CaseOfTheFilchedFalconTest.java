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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
