package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClockworkVorrac.class, YotianSoldier.class, Ornithopter.class})
class ClockworkVorracTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithCounters() {
        Permanent vorrac = castVorrac();

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Tap ability puts a +1/+1 counter on it")
    void activatedAbilityAddsCounter() {
        Permanent vorrac = castVorrac();
        vorrac.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(vorrac.isTapped()).isTrue();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Attacking removes a +1/+1 counter at end of combat")
    void attackingRemovesCounterAtEndOfCombat() {
        Permanent vorrac = addCreatureReady(player1, new ClockworkVorrac());
        vorrac.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocking removes a +1/+1 counter at end of combat")
    void blockingRemovesCounterAtEndOfCombat() {
        addCreatureReady(player1, new YotianSoldier());
        Permanent vorrac = addCreatureReady(player2, new ClockworkVorrac());
        vorrac.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        resolveCombat();

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not remove a counter when it neither attacks nor blocks")
    void doesNothingWhenNotInCombat() {
        Permanent vorrac = addCreatureReady(player1, new ClockworkVorrac());
        vorrac.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        declareAttackers(List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(vorrac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent vorrac = addCreatureReady(player1, new ClockworkVorrac());
        vorrac.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent blocker = addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    private Permanent castVorrac() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ClockworkVorrac(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Clockwork Vorrac");
    }
}
