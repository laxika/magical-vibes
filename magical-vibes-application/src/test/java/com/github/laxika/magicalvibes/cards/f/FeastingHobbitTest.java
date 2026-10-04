package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeastingHobbit.class, FeastingTrollKing.class, GrizzlyBears.class, LlanowarElves.class})
class FeastingHobbitTest extends BaseCardTest {

    @Test
    @DisplayName("Devours any number of Foods and gets three counters per Food")
    void devoursFoodsForCounters() {
        castTrollKing();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Permanent> foods = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Food"))
                .toList();

        castHobbit();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, foods.stream().map(Permanent::getId).toList());

        Permanent hobbit = findPermanent(player1, "Feasting Hobbit");
        assertThat(hobbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(9);
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("Does not offer non-Food permanents for devour")
    void doesNotOfferNonFoods() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHobbit();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Feasting Hobbit")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be blocked by a creature with less power")
    void cannotBeBlockedByLowerPowerCreature() {
        Permanent hobbit = harness.addToBattlefieldAndReturn(player1, new FeastingHobbit());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        hobbit.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(hobbit);

        assertThatThrownBy(() -> gs.declareBlockers(
                        gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("May decline to devour even when Foods are available")
    void mayDeclineDevour() {
        castTrollKing();
        castHobbit();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(findPermanent(player1, "Feasting Hobbit")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May devour only one of several available Foods")
    void mayDevourOnlySomeFoods() {
        castTrollKing();
        Permanent food = findPermanent(player1, "Food");
        castHobbit();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(food.getId()));

        assertThat(findPermanent(player1, "Feasting Hobbit")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
    }

    @Test
    @DisplayName("Cannot devour Foods controlled by an opponent")
    void cannotDevourOpponentsFoods() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new FeastingTrollKing(), "{2}{G}{G}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        castHobbit();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Feasting Hobbit")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player2, "Food")).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature with equal power may block")
    void equalPowerCreatureMayBlock() {
        Permanent hobbit = harness.addToBattlefieldAndReturn(player1, new FeastingHobbit());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FeastingHobbit());
        hobbit.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(hobbit))));

        assertThat(blocker.getBlockingTargetIds()).contains(hobbit.getId());
    }

    @Test
    @DisplayName("Devour counters increase the power required to block")
    void devourCountersAffectBlockingRestriction() {
        castTrollKing();
        Permanent food = findPermanent(player1, "Food");
        castHobbit();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(food.getId()));

        Permanent hobbit = findPermanent(player1, "Feasting Hobbit");
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FeastingHobbit());
        hobbit.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(hobbit)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    private void castTrollKing() {
        harness.castFromHand(player1, new FeastingTrollKing(), "{2}{G}{G}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castHobbit() {
        harness.castFromHand(player1, new FeastingHobbit(), "{1}{G}");
    }
}
