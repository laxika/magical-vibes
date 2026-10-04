package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleetfeatherCockatrice.class, GoldenHind.class})
class FleetfeatherCockatriceTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Fleetfeather Cockatrice")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent cockatrice = addCreatureReady(player1, new FleetfeatherCockatrice());
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cockatrice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cockatrice.isMonstrous()).isTrue();
        assertThat(cockatrice.getEffectivePower()).isEqualTo(6);
        assertThat(cockatrice.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Monstrosity can be activated again but adds no more counters")
    void monstrosityCanBeActivatedAgainWithoutAddingCounters() {
        Permanent cockatrice = addCreatureReady(player1, new FleetfeatherCockatrice());
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(cockatrice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cockatrice.isMonstrous()).isTrue();
    }

    @Test
    void multiplePendingActivationsOnlyAddCountersOnce() {
        Permanent cockatrice = addCreatureReady(player1, new FleetfeatherCockatrice());
        addMonstrosityMana(player1);
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(cockatrice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cockatrice.isMonstrous()).isTrue();
    }

    @Test
    void canCastWithFlashDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FleetfeatherCockatrice(), "{3}{G}{U}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fleetfeather Cockatrice");
    }

    @Test
    void monstrosityWorksWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent cockatrice = harness.addToBattlefieldAndReturn(player1, new FleetfeatherCockatrice());
        cockatrice.setSummoningSick(true);
        cockatrice.setTapped(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        addMonstrosityMana(player1);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cockatrice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cockatrice.isMonstrous()).isTrue();
        assertThat(cockatrice.isTapped()).isTrue();
    }

    @Test
    void groundCreatureCannotBlockCockatrice() {
        addCreatureReady(player1, new FleetfeatherCockatrice());
        harness.addToBattlefield(player2, new GoldenHind());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void deathtouchKillsMonstrousFlyingBlockerWithNonlethalDamage() {
        addCreatureReady(player1, new FleetfeatherCockatrice());
        addCreatureReady(player2, new FleetfeatherCockatrice());
        addMonstrosityMana(player2);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Fleetfeather Cockatrice");
        harness.assertInGraveyard(player2, "Fleetfeather Cockatrice");
        harness.assertLife(player2, 20);
    }

    private void addMonstrosityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
