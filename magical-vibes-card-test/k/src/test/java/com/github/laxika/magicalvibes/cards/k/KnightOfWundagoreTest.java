package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheRuinousWreckingCrew;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfWundagore.class, BurstOfStrength.class, GrizzlyBears.class, TheRuinousWreckingCrew.class})
class KnightOfWundagoreTest extends BaseCardTest {

    @Test
    void putsACounterOnItselfWhenYouPutOneOnAnotherCreature() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(player1, creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersOnlyOnceEachTurnAndNotForPuttingACounterOnItself() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(player1, knight);
        resolveAllTriggers();
        putCounterOn(player1, firstCreature);
        resolveAllTriggers();
        putCounterOn(player1, secondCreature);
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersWhenYouPutACounterOnAnOpponentsCreature() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        putCounterOn(player1, opponentCreature);
        resolveAllTriggers();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenAnOpponentPutsTheCounterOnYourCreature() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(player2, creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canTriggerAgainOnTheOpponentsTurn() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(player1, creature);
        resolveAllTriggers();
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        putCounterOn(player1, creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void twoKnightsTriggerEachOtherOnlyOnceEach() {
        Permanent firstKnight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent secondKnight = addCreatureReady(player1, new KnightOfWundagore());

        putCounterOn(player1, firstKnight);
        resolveAllTriggers();

        assertThat(firstKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void aNewKnightCanTriggerAfterAnotherKnightHasAlreadyTriggeredThisTurn() {
        Permanent firstKnight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        putCounterOn(player1, creature);
        resolveAllTriggers();

        Permanent secondKnight = addCreatureReady(player1, new KnightOfWundagore());
        putCounterOn(player1, creature);
        resolveAllTriggers();

        assertThat(firstKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void triggersWhenAnotherCreatureEntersWithPlusOnePlusOneCounters() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        harness.setHand(player1, List.of(new TheRuinousWreckingCrew()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Target opponent loses 2 life.");
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        Permanent crew = findPermanent(player1, "The Ruinous Wrecking Crew");
        assertThat(crew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void putCounterOn(Player player, Permanent target) {
        harness.setHand(player, List.of(new BurstOfStrength()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player, 0, target.getId());
    }
}
