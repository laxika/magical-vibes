package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhoxMaulers.class, GiantSpider.class, TimberpackWolf.class, Disperse.class})
class RhoxMaulersTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 2 puts two +1/+1 counters on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent maulers = addCreatureReady(player1, new RhoxMaulers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(maulers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(maulers.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent maulers = addCreatureReady(player1, new RhoxMaulers());
        maulers.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(maulers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A blocker that soaks all the damage leaves no trample damage and no renown")
    void fullyBlockedDoesNotTriggerRenown() {
        Permanent maulers = addCreatureReady(player1, new RhoxMaulers());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(maulers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(maulers.isRenowned()).isFalse();
    }

    @Test
    @DisplayName("Trample damage to the defending player triggers renown")
    void trampleDamageTriggersRenown() {
        Permanent maulers = addCreatureReady(player1, new RhoxMaulers());
        addCreatureReady(player2, new TimberpackWolf());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Timberpack Wolf");
        assertThat(maulers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(maulers.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Returning the source to hand in response prevents renown from affecting it")
    void sourceLeavesBeforeRenownResolves() {
        Permanent maulers = addCreatureReady(player1, new RhoxMaulers());
        maulers.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).hasSize(1);
        assertThat(maulers.isRenowned()).isFalse();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, maulers.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Rhox Maulers");
        harness.assertNotOnBattlefield(player1, "Rhox Maulers");
        assertThat(maulers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(maulers.isRenowned()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
