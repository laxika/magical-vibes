package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandraAcolyteOfFlame;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VivienArkbowRanger.class, GreenwoodSentinel.class, ChandraAcolyteOfFlame.class, Forest.class})
class VivienArkbowRangerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 distributes counters among two creatures and grants trample until end of turn")
    void plusOneDistributesCountersAndGrantsTrample() {
        Permanent vivien = addReadyVivien(5);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isFalse();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 makes a controlled creature deal its power to a planeswalker")
    void minusThreeDealsPowerDamageToPlaneswalker() {
        Permanent vivien = addReadyVivien(5);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraAcolyteOfFlame());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        planeswalker.setSummoningSick(false);

        harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 rejects a player as its second target")
    void minusThreeRejectsPlayerTarget() {
        addReadyVivien(5);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-5 offers only outside-the-game creature cards and puts the chosen card into hand")
    void minusFiveSearchesSideboardForCreature() {
        addReadyVivien(5);
        Card creature = new GreenwoodSentinel();
        Card nonCreature = new Forest();
        gd.playerSideboards.put(player1.getId(), new java.util.ArrayList<>(List.of(creature, nonCreature)));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().cards()).containsExactly(creature);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void plusOneCanChooseNoTargets() {
        Permanent vivien = addReadyVivien(5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOnePutsBothCountersOnOneOpposingCreature() {
        addReadyVivien(5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void plusOneDoesNotRedistributeCounterFromRemovedTarget() {
        addReadyVivien(5);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void minusThreeUsesCurrentPowerAndDoesNotDealDamageBack() {
        addReadyVivien(5);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        victim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), victim.getId()));
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(3);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void minusThreeRejectsAnOpposingDamageSource() {
        addReadyVivien(5);
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusThreeDealsNoDamageWhenSourceChangesController() {
        addReadyVivien(5);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(victim);
    }

    @Test
    void minusFiveCanBeDeclined() {
        addReadyVivien(5);
        Card creature = new GreenwoodSentinel();
        gd.playerSideboards.put(player1.getId(), new java.util.ArrayList<>(List.of(creature)));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusFiveCannotTakeOpponentsOutsideGameCreature() {
        addReadyVivien(5);
        Card creature = new GreenwoodSentinel();
        gd.playerSideboards.put(player1.getId(), new java.util.ArrayList<>(List.of(new Forest())));
        gd.playerSideboards.put(player2.getId(), new java.util.ArrayList<>(List.of(creature)));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private Permanent addReadyVivien(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new VivienArkbowRanger());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
