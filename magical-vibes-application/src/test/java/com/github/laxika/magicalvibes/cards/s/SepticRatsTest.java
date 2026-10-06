package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SepticRats.class})
class SepticRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the trigger on the stack when the defending player is poisoned")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new SepticRats());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Septic Rats"));
    }

    @Test
    @DisplayName("Gets +1/+1 when attacking if defending player is poisoned")
    void boostsWhenDefendingPlayerPoisoned() {
        Permanent rats = addCreatureReady(player1, new SepticRats());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(rats.getPowerModifier()).isEqualTo(1);
        assertThat(rats.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does NOT get +1/+1 when attacking if defending player has no poison counters")
    void noBoostWhenDefendingPlayerNotPoisoned() {
        Permanent rats = addCreatureReady(player1, new SepticRats());
        // No poison counters on defender

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(rats.getPowerModifier()).isEqualTo(0);
        assertThat(rats.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Checks defending player (opponent), not controller for poison")
    void checksDefendingPlayerNotController() {
        Permanent rats = addCreatureReady(player1, new SepticRats());
        // Controller has poison but defender does not
        gd.playerPoisonCounters.put(player1.getId(), 3);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(rats.getPowerModifier()).isEqualTo(0);
        assertThat(rats.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Losing the last poison counter before resolution prevents the boost")
    void rechecksPoisonAtResolution() {
        Permanent rats = addCreatureReady(player1, new SepticRats());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);

        gd.playerPoisonCounters.put(player2.getId(), 0);
        resolveAllTriggers();

        assertThat(rats.getPowerModifier()).isZero();
        assertThat(rats.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An unpoisoned defender takes infect damage without granting an attack boost")
    void firstAttackDealsTwoPoisonCounters() {
        Permanent rats = addCreatureReady(player1, new SepticRats());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(rats.getPowerModifier()).isZero();
        assertThat(rats.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Infect combat damage leaves a surviving blocker with reduced stats and no marked damage")
    void infectDamageToCreature() {
        Permanent blocker = addCreatureReady(player2, new SepticRats());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new SepticRats());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Septic Rats");
        harness.assertOnBattlefield(player2, "Septic Rats");
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Boosted infect damage adds three poison counters and the boost expires after the turn")
    void boostedDamageAndEndOfTurnExpiry() {
        Permanent rats = addCreatureReady(player1, new SepticRats());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(rats.getPowerModifier()).isEqualTo(1);
        assertThat(rats.getToughnessModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(rats.getPowerModifier()).isZero();
        assertThat(rats.getToughnessModifier()).isZero();
    }
}
