package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InspiringRefrain;
import com.github.laxika.magicalvibes.cards.r.RoryWilliams;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmyPond.class, RoryWilliams.class, InspiringRefrain.class, Forest.class})
class AmyPondTest extends BaseCardTest {

    @Test
    void partnerWithRoryLetsTargetPlayerSearchTheirLibrary() {
        RoryWilliams rory = new RoryWilliams();
        Forest decoy = new Forest();
        harness.setLibrary(player2, List.of(decoy, rory));
        harness.setHand(player1, List.of(new AmyPond()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(rory);
    }

    @Test
    void combatDamageRemovesThatManyCountersFromOneOwnedSuspendedCard() {
        AmyPond amy = new AmyPond();
        addCreatureReady(player1, amy);
        InspiringRefrain vision = new InspiringRefrain();
        harness.setExile(player1, List.of(vision));
        gd.setExiledCardCounters(vision.getId(), CounterType.TIME, 5);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SuspendedCardTimeCounterChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(vision.getId()));

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).containsEntry(vision.getId(), 3);
    }

    @Test
    void targetPlayerMayDeclinePartnerSearch() {
        RoryWilliams rory = new RoryWilliams();
        harness.setLibrary(player2, List.of(rory));
        harness.setHand(player1, List.of(new AmyPond()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(rory);
        harness.assertNotInHand(player2, "Rory Williams");
    }

    @Test
    void choosesOnlyOneOwnedSuspendedCard() {
        addCreatureReady(player1, new AmyPond());
        InspiringRefrain first = new InspiringRefrain();
        InspiringRefrain second = new InspiringRefrain();
        InspiringRefrain opposing = new InspiringRefrain();
        InspiringRefrain withoutCounters = new InspiringRefrain();
        harness.setExile(player1, List.of(first, second, withoutCounters));
        harness.setExile(player2, List.of(opposing));
        gd.setExiledCardCounters(first.getId(), CounterType.TIME, 5);
        gd.setExiledCardCounters(second.getId(), CounterType.TIME, 4);
        gd.setExiledCardCounters(opposing.getId(), CounterType.TIME, 5);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SuspendedCardTimeCounterChoice.class);
        var choice = (PendingInteraction.SuspendedCardTimeCounterChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).containsEntry(first.getId(), 5)
                .containsEntry(second.getId(), 2).containsEntry(opposing.getId(), 5);
    }

    @Test
    void noSuspendedCardsMakesCombatTriggerDoNothing() {
        addCreatureReady(player1, new AmyPond());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void exiledCardsWithTimeCountersButWithoutSuspendCannotBeChosen() {
        addCreatureReady(player1, new AmyPond());
        AmyPond exiled = new AmyPond();
        harness.setExile(player1, List.of(exiled));
        gd.setExiledCardCounters(exiled.getId(), CounterType.TIME, 3);
        gd.exiledCardsWithNonSuspendTimeCounters.add(exiled.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).containsEntry(exiled.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canRemoveCountersFromRorySuspendedByHisCastTrigger() {
        addCreatureReady(player1, new AmyPond());
        RoryWilliams rory = new RoryWilliams();
        harness.setHand(player1, List.of(rory));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).contains(
                Map.entry(rory.getId(), 3));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SuspendedCardTimeCounterChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(rory.getId()));

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).contains(
                Map.entry(rory.getId(), 1));
    }

    @Test
    void removingLastCounterCreatesRespondableSuspendCastTrigger() {
        addCreatureReady(player1, new AmyPond());
        InspiringRefrain refrain = new InspiringRefrain();
        harness.setExile(player1, List.of(refrain));
        gd.setExiledCardCounters(refrain.getId(), CounterType.TIME, 1);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(refrain.getId()));

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).doesNotContainKey(refrain.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.findExiledCard(refrain.getId())).isNotNull();
    }

    @Test
    void removingLastGrantedSuspendCounterCreatesRespondableCastTrigger() {
        addCreatureReady(player1, new AmyPond());
        RoryWilliams rory = new RoryWilliams();
        harness.setExile(player1, List.of(rory));
        gd.setExiledCardCounters(rory.getId(), CounterType.TIME, 1);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(rory.getId()));

        assertThat(gd.exiledCardsWithCounter(CounterType.TIME)).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.findExiledCard(rory.getId())).isNotNull();
    }
}
