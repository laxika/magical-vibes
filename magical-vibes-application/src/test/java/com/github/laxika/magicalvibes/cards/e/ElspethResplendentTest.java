package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElspethResplendent.class, GrizzlyBears.class, HillGiant.class, Shock.class, WitnessProtection.class})
class ElspethResplendentTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a +1/+1 counter and the chosen keyword counter on a creature")
    void plusOneAddsCountersAndVigilance() {
        addReadyElspeth(4);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Vigilance");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("+1 may choose no creature")
    void plusOneMayChooseNoCreature() {
        Permanent elspeth = addReadyElspeth(4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleListChoice(player1, "Vigilance");
        }

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("-3 puts an eligible permanent onto the battlefield with a shield counter")
    void minusThreePutsEligiblePermanentOntoBattlefieldWithShieldCounter() {
        addReadyElspeth(3);
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        Card nonPermanent = new Shock();
        harness.setLibrary(player1, List.of(eligible, tooExpensive, nonPermanent));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tooExpensive, nonPermanent);
    }

    @Test
    @DisplayName("-7 creates five flying Angel tokens")
    void minusSevenCreatesFiveAngels() {
        addReadyElspeth(7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(5);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
    }

    @ParameterizedTest
    @CsvSource({"Flying, FLYING", "First strike, FIRST_STRIKE", "Lifelink, LIFELINK"})
    void plusOneAddsOtherKeywordCounters(String choice, CounterType counterType) {
        addReadyElspeth(4);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, choice);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(counterType)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.valueOf(counterType.name()))).isTrue();
    }

    @Test
    void minusThreeMayDeclineEligiblePermanent() {
        addReadyElspeth(4);
        Card eligible = new GrizzlyBears();
        Card other = new Shock();
        harness.setLibrary(player1, List.of(eligible, other));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(eligible, other);
    }

    @Test
    void plusOneDoesNothingWhenItsTargetLeavesBeforeResolution() {
        Permanent elspeth = addReadyElspeth(4);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusThreeLooksAtOnlySevenCardsAndBottomsThemBelowTheRest() {
        addReadyElspeth(4);
        List<Card> topSeven = java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> (Card) new Shock()).toList();
        Card eighth = new GrizzlyBears();
        List<Card> library = new java.util.ArrayList<>(topSeven);
        library.add(eighth);
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(eighth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 8))
                .containsExactlyInAnyOrderElementsOf(topSeven);
    }

    @Test
    void minusThreePutsAuraOntoBattlefieldAttachedWithShieldCounter() {
        addReadyElspeth(4);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Card aura = new WitnessProtection();
        harness.setLibrary(player1, List.of(aura));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        Permanent entered = findPermanent(player1, "Witness Protection");
        assertThat(entered.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(entered.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void minusThreeLeavesAuraInLibraryWhenThereIsNoLegalHost() {
        addReadyElspeth(4);
        Card aura = new WitnessProtection();
        harness.setLibrary(player1, List.of(aura));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        harness.assertNotOnBattlefield(player1, "Witness Protection");
        harness.assertNotInGraveyard(player1, "Witness Protection");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
    }

    private Permanent addReadyElspeth(int loyalty) {
        Permanent permanent = addCreatureReady(player1, new ElspethResplendent());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
