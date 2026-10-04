package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.model.Card;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirstDayOfClass.class, EnvironmentalSciences.class, SpinedKarok.class})
class FirstDayOfClassTest extends BaseCardTest {

    @Test
    @DisplayName("Gives each own creature entering this turn a +1/+1 counter and haste")
    void boostsOwnEnteringCreature() {
        castFirstDayOfClass();

        harness.setHand(player1, List.of(new SpinedKarok()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Spined Karok");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not affect an opponent's creature entering")
    void doesNotAffectOpponentsCreature() {
        castFirstDayOfClass();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SpinedKarok()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Spined Karok");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The haste grant wears off at end of turn but the counter remains")
    void hasteWearsOffAtEndOfTurn() {
        castFirstDayOfClass();

        harness.setHand(player1, List.of(new SpinedKarok()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Spined Karok");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Learn can discard a card and draw a card")
    void learnDiscardsAndDraws() {
        Card discarded = new SpinedKarok();
        Card drawn = new EnvironmentalSciences();
        harness.setHand(player1, List.of(new FirstDayOfClass(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("A creature already on the battlefield is not affected")
    void doesNotAffectExistingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());

        castFirstDayOfClass();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The delayed ability triggers for multiple creatures during the turn")
    void boostsMultipleEnteringCreatures() {
        castFirstDayOfClass();

        Permanent first = harness.enterBattlefieldAndReturn(player1, new SpinedKarok());
        harness.passBothPriorities();
        Permanent second = harness.enterBattlefieldAndReturn(player1, new SpinedKarok());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Multiple resolutions give each entering creature one counter per delayed ability")
    void multipleResolutionsStack() {
        castFirstDayOfClass();
        castFirstDayOfClass();

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new SpinedKarok());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The delayed ability expires before creatures enter next turn")
    void doesNotBoostCreatureNextTurn() {
        castFirstDayOfClass();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new SpinedKarok());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining both learn options still registers the delayed ability")
    void decliningLearnStillBoostsCreature() {
        Card retained = new SpinedKarok();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new FirstDayOfClass(), retained));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new SpinedKarok());
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private void castFirstDayOfClass() {
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new FirstDayOfClass()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }
}
