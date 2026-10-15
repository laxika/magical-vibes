package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.g.GraniteShard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlithStrider.class, AlphaMyr.class, GraniteShard.class})
class SlithStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it becomes blocked")
    void drawsCardWhenBlocked() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new AlphaMyr())));

        addCreatureReady(player1, new SlithStrider());
        addCreatureReady(player2, new AlphaMyr());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws only one card when blocked by multiple creatures")
    void drawsOnlyOneCardWhenBlockedByMultipleCreatures() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new AlphaMyr(), new AlphaMyr(), new AlphaMyr()));

        addCreatureReady(player1, new SlithStrider());
        addCreatureReady(player2, new AlphaMyr());
        addCreatureReady(player2, new AlphaMyr());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when it deals combat damage to a player")
    void getsCounterOnCombatDamageToPlayer() {
        Permanent strider = addCreatureReady(player1, new SlithStrider());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(strider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a +1/+1 counter from noncombat damage to a player")
    void noCounterOnNoncombatDamageToPlayer() {
        Permanent strider = addCreatureReady(player1, new SlithStrider());
        addCreatureReady(player1, new GraniteShard());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(strider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Draw resolves before combat damage and does not grant a counter for damaging a blocker")
    void drawsBeforeCombatDamageWithoutCounterForCreatureDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlphaMyr(), new AlphaMyr(), new AlphaMyr()));
        Permanent strider = addCreatureReady(player1, new SlithStrider());
        addCreatureReady(player2, new AlphaMyr());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(strider);
        assertThat(strider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Slith Strider");
        harness.assertInGraveyard(player2, "Alpha Myr");
        assertThat(strider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Draw trigger resolves even if Slith Strider is destroyed in response")
    void drawsAfterSourceLeavesBattlefield() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlphaMyr(), new AlphaMyr()));
        Permanent strider = addCreatureReady(player1, new SlithStrider());
        addCreatureReady(player2, new AlphaMyr());
        harness.addToBattlefield(player2, new GraniteShard());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.activateAbility(player2, 1, null, strider.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        harness.assertInGraveyard(player1, "Slith Strider");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An unblocked attack grants a counter but does not draw a card")
    void unblockedAttackDoesNotDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlphaMyr(), new AlphaMyr()));
        Permanent strider = addCreatureReady(player1, new SlithStrider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(strider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
