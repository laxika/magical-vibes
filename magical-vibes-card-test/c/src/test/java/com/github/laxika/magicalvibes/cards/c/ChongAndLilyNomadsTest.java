package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FoundingOfOmashu;
import com.github.laxika.magicalvibes.cards.h.HippoCows;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChongAndLilyNomads.class, FoundingOfOmashu.class, HippoCows.class})
class ChongAndLilyNomadsTest extends BaseCardTest {

    private static final String PUT_LORE_COUNTERS =
            "Put a lore counter on each of any number of target Sagas you control.";
    private static final String BOOST_CREATURES =
            "Creatures you control get +1/+0 until end of turn for each lore counter among Sagas you control.";

    @Test
    void bardAttackCanPutLoreCountersOnAnyNumberOfOwnedSagas() {
        addCreatureReady(player1, new ChongAndLilyNomads());
        addCreatureReady(player1, creature("Bard", CardSubtype.BARD, 2, 2));
        Permanent firstSaga = addSaga(player1, 0);
        Permanent secondSaga = addSaga(player1, 1);
        Permanent opponentSaga = addSaga(player2, 0);

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, PUT_LORE_COUNTERS);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();

        harness.handlePermanentChosen(player1, firstSaga.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(secondSaga.getId())
                .doesNotContain(opponentSaga.getId());
        harness.handlePermanentChosen(player1, secondSaga.getId());
        harness.passBothPriorities();

        assertThat(firstSaga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(secondSaga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(opponentSaga.getCounterCount(CounterType.LORE)).isZero();
    }

    @Test
    void boostModeUsesLoreCountersOnControlledSagas() {
        Permanent chongAndLily = addCreatureReady(player1, new ChongAndLilyNomads());
        Permanent bard = addCreatureReady(player1, creature("Bard", CardSubtype.BARD, 2, 2));
        addSaga(player1, 2);

        declareAttackers(List.of(1));
        harness.handleListChoice(player1, BOOST_CREATURES);
        harness.passBothPriorities();

        assertThat(chongAndLily.getPowerModifier()).isEqualTo(2);
        assertThat(bard.getPowerModifier()).isEqualTo(2);
        assertThat(chongAndLily.getToughnessModifier()).isZero();
        assertThat(bard.getToughnessModifier()).isZero();
    }

    @Test
    void attackWithoutABardDoesNotTrigger() {
        addCreatureReady(player1, new ChongAndLilyNomads());
        addCreatureReady(player1, creature("Warrior", CardSubtype.WARRIOR, 2, 2));
        addSaga(player1, 0);

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleAttackingBardsTriggerOnlyOnce() {
        Permanent source = addCreatureReady(player1, new ChongAndLilyNomads());
        addCreatureReady(player1, creature("Bard", CardSubtype.BARD, 2, 2));
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FoundingOfOmashu());
        saga.setCounterCount(CounterType.LORE, 1);

        declareAttackers(List.of(0, 1));
        harness.handleListChoice(player1, BOOST_CREATURES);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(source.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void boostCountsLoreAtResolutionAndSurvivesSourceLeaving() {
        Permanent source = addCreatureReady(player1, new ChongAndLilyNomads());
        Permanent creature = addCreatureReady(player1, new HippoCows());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FoundingOfOmashu());
        saga.setCounterCount(CounterType.LORE, 1);
        Permanent opposingSaga = harness.addToBattlefieldAndReturn(player2, new FoundingOfOmashu());
        opposingSaga.setCounterCount(CounterType.LORE, 2);
        Permanent opposingCreature = addCreatureReady(player2, new HippoCows());
        creature.setCounterCount(CounterType.LORE, 4);

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, BOOST_CREATURES);
        saga.setCounterCount(CounterType.LORE, 2);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(opposingCreature.getPowerModifier()).isZero();
        saga.setCounterCount(CounterType.LORE, 1);
        assertThat(creature.getPowerModifier()).isEqualTo(2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    void boostWithoutControlledSagasDoesNothing() {
        Permanent source = addCreatureReady(player1, new ChongAndLilyNomads());
        Permanent opposingSaga = harness.addToBattlefieldAndReturn(player2, new FoundingOfOmashu());
        opposingSaga.setCounterCount(CounterType.LORE, 2);

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, BOOST_CREATURES);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
    }

    @Test
    void loreModeSkipsSagaNoLongerControlledButCountersRemainingTarget() {
        addCreatureReady(player1, new ChongAndLilyNomads());
        Permanent firstSaga = harness.addToBattlefieldAndReturn(player1, new FoundingOfOmashu());
        Permanent secondSaga = harness.addToBattlefieldAndReturn(player1, new FoundingOfOmashu());

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, PUT_LORE_COUNTERS);
        harness.handlePermanentChosen(player1, firstSaga.getId());
        harness.handlePermanentChosen(player1, secondSaga.getId());
        gd.playerBattlefields.get(player1.getId()).remove(firstSaga);
        gd.playerBattlefields.get(player2.getId()).add(firstSaga);
        harness.passBothPriorities();

        assertThat(firstSaga.getCounterCount(CounterType.LORE)).isZero();
        assertThat(secondSaga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Ally")).isEqualTo(2);
    }

    @Test
    void loreModeCanChooseZeroTargetsEvenWithSagaAvailable() {
        addCreatureReady(player1, new ChongAndLilyNomads());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FoundingOfOmashu());
        saga.setCounterCount(CounterType.LORE, 1);

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, PUT_LORE_COUNTERS);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void loreModeAllowsMoreThanNinetyNineSagaTargets() {
        addCreatureReady(player1, new ChongAndLilyNomads());
        List<Permanent> sagas = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            sagas.add(harness.addToBattlefieldAndReturn(player1, new FoundingOfOmashu()));
        }

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, PUT_LORE_COUNTERS);
        for (int i = 0; i < 99; i++) {
            harness.handlePermanentChosen(player1, sagas.get(i).getId());
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(sagas.get(99).getId());
        harness.handlePermanentChosen(player1, sagas.get(99).getId());
        harness.passBothPriorities();
        assertThat(sagas).allSatisfy(saga ->
                assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1));
    }

    private Permanent addSaga(Player player, int loreCounters) {
        Card saga = new Card();
        saga.setName("Test Saga");
        saga.setType(CardType.ENCHANTMENT);
        saga.setSubtypes(List.of(CardSubtype.SAGA));
        Permanent permanent = harness.addToBattlefieldAndReturn(player, saga);
        permanent.setCounterCount(CounterType.LORE, loreCounters);
        return permanent;
    }

    private Card creature(String name, CardSubtype subtype, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
