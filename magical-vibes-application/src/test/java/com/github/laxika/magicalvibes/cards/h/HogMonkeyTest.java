package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HogMonkey.class, GrizzlyBears.class})
@DisplayName("Hog-Monkey")
class HogMonkeyTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat gives menace to a countered creature you control until end of turn")
    void beginningOfCombatGivesMenaceToCounteredCreature() {
        addCreatureReady(player1, new HogMonkey());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId())
                .doesNotContain(uncountered.getId(), opponent.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.getGameService().advanceStep(gd);

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Exhaust puts two +1/+1 counters on Hog-Monkey and can be activated only once")
    void exhaustPutsTwoCountersOnItOnlyOnce() {
        Permanent monkey = addCreatureReady(player1, new HogMonkey());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monkey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Hog-Monkey can target itself after exhausting")
    void canTargetItselfAfterExhausting() {
        Permanent monkey = addCreatureReady(player1, new HogMonkey());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToCombat();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(monkey.getId());
        harness.handlePermanentChosen(player1, monkey.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, monkey, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The combat trigger does not trigger on an opponent's turn")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent monkey = addCreatureReady(player1, new HogMonkey());
        monkey.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, monkey, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The combat trigger has no legal target without a +1/+1 counter")
    void noTriggerIsStackedWithoutLegalTarget() {
        Permanent monkey = addCreatureReady(player1, new HogMonkey());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, monkey, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Losing the last +1/+1 counter before resolution makes the target illegal")
    void losingCounterBeforeResolutionPreventsMenace() {
        Permanent monkey = addCreatureReady(player1, new HogMonkey());
        monkey.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        advanceToCombat();
        harness.handlePermanentChosen(player1, monkey.getId());
        monkey.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, monkey, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Losing the counter after resolution does not remove menace")
    void losingCounterAfterResolutionKeepsMenace() {
        Permanent monkey = addCreatureReady(player1, new HogMonkey());
        monkey.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        advanceToCombat();
        harness.handlePermanentChosen(player1, monkey.getId());
        harness.passBothPriorities();
        monkey.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, monkey, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Exhaust can be activated while tapped and summoning sick, but not twice on the stack")
    void exhaustWorksWhileTappedAndSummoningSick() {
        Permanent monkey = harness.addToBattlefieldAndReturn(player1, new HogMonkey());
        monkey.setSummoningSick(true);
        monkey.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();

        assertThat(monkey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(monkey.isTapped()).isTrue();
    }

    private void advanceToCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
