package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.m.MortalObstinacy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AjaniMentorOfHeroes.class, GoldenHind.class, MortalObstinacy.class,
        FontOfFertility.class})
class AjaniMentorOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("First +1 distributes three counters among controlled creatures")
    void firstPlusOneDistributesCountersAmongControlledCreatures() {
        Permanent ajani = addReadyAjani(4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoldenHind());

        harness.ensurePriority(player1);
        harness.getGameService().activateAbility(
                gd, player1, 0, 0, null, null, null,
                List.of(first.getId(), second.getId()),
                Map.of(first.getId(), 2, second.getId(), 1));
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First +1 rejects an opponent's creature")
    void firstPlusOneRejectsOpponentsCreature() {
        addReadyAjani(4);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.getGameService().activateAbility(
                gd, player1, 0, 0, null, null, null,
                List.of(opponentCreature.getId()),
                Map.of(opponentCreature.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second +1 offers only Aura, creature, and planeswalker cards")
    void secondPlusOneOffersMatchingCardTypes() {
        Permanent ajani = addReadyAjani(4);
        Card aura = new MortalObstinacy();
        Card creature = new GoldenHind();
        Card planeswalker = new AjaniMentorOfHeroes();
        Card enchantment = new FontOfFertility();
        harness.setLibrary(player1, List.of(aura, creature, planeswalker, enchantment));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).extracting(Card::getId).containsExactlyInAnyOrder(
                aura.getId(), creature.getId(), planeswalker.getId());
        assertThat(choice.params().cards()).extracting(Card::getId).doesNotContain(enchantment.getId());

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(choice.params().cards().indexOf(creature)));
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Ultimate gains 100 life")
    void ultimateGainsOneHundredLife() {
        Permanent ajani = addReadyAjani(8);
        gd.playerLifeTotals.put(player1.getId(), 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(107);
    }

    @Test
    void firstPlusOneCanPutAllThreeCountersOnOneCreature() {
        addReadyAjani(4);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.ensurePriority(player1);
        gs.activateAbility(gd, player1, 0, 0, null, null, null,
                List.of(creature.getId()), Map.of(creature.getId(), 3));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void firstPlusOneDoesNotAffectATargetThatChangesController() {
        addReadyAjani(4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.ensurePriority(player1);
        gs.activateAbility(gd, player1, 0, 0, null, null, null,
                List.of(first.getId(), second.getId()),
                Map.of(first.getId(), 2, second.getId(), 1));

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).add(first);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void secondPlusOneWithAnEmptyLibraryStillAddsLoyalty() {
        Permanent ajani = addReadyAjani(4);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void firstPlusOneKeepsAssignmentsWhenOneTargetLeaves() {
        addReadyAjani(4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.ensurePriority(player1);
        gs.activateAbility(gd, player1, 0, 0, null, null, null,
                List.of(first.getId(), second.getId()),
                Map.of(first.getId(), 2, second.getId(), 1));

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void firstPlusOneDistributesOneCounterToEachOfThreeTargets() {
        addReadyAjani(4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.ensurePriority(player1);
        gs.activateAbility(gd, player1, 0, 0, null, null, null,
                List.of(first.getId(), second.getId(), third.getId()),
                Map.of(first.getId(), 1, second.getId(), 1, third.getId(), 1));
        harness.passBothPriorities();

        for (Permanent creature : List.of(first, second, third)) {
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }
    }

    @Test
    void firstPlusOneRejectsIncompleteDistribution() {
        Permanent ajani = addReadyAjani(4);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.activateAbility(gd, player1, 0, 0, null, null, null,
                List.of(creature.getId()), Map.of(creature.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondPlusOneMayDeclineAndBottomAllFourInChosenOrder() {
        addReadyAjani(4);
        Card first = new GoldenHind();
        Card second = new MortalObstinacy();
        Card third = new AjaniMentorOfHeroes();
        Card fourth = new FontOfFertility();
        Card untouched = new GoldenHind();
        harness.setLibrary(player1, List.of(first, second, third, fourth, untouched));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouched, fourth, third, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void secondPlusOneCanSelectFromAShortLibrary() {
        addReadyAjani(4);
        Card creature = new GoldenHind();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void secondPlusOneWithNoMatchingCardsBottomsThemInChosenOrder() {
        addReadyAjani(4);
        Card first = new FontOfFertility();
        Card second = new FontOfFertility();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyAjani(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new AjaniMentorOfHeroes());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
