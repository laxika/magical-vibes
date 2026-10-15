package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LiveFast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialGorger.class, LiveFast.class})
class TerritorialGorgerTest extends BaseCardTest {

    @Test
    void getsOneBoostForAnEnergyGainEvent() {
        Permanent gorger = addCreatureReady(player1, new TerritorialGorger());
        harness.setLibrary(player1, List.of(new LiveFast(), new LiveFast(), new LiveFast()));
        harness.setHand(player1, List.of(new LiveFast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(4);
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent gorger = addCreatureReady(player1, new TerritorialGorger());
        harness.setLibrary(player1, List.of(new LiveFast(), new LiveFast(), new LiveFast()));
        harness.setHand(player1, List.of(new LiveFast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        resolveAllTriggers();
        assertThat(gorger.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gorger.getPowerModifier()).isZero();
        assertThat(gorger.getToughnessModifier()).isZero();
    }

    @Test
    void boostWaitsForTriggeredAbilityToResolve() {
        Permanent gorger = addCreatureReady(player1, new TerritorialGorger());
        harness.setLibrary(player1, List.of(new LiveFast(), new LiveFast()));
        harness.setHand(player1, List.of(new LiveFast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gorger.getPowerModifier()).isZero();
        assertThat(gorger.getToughnessModifier()).isZero();

        resolveAllTriggers();

        assertThat(gorger.getPowerModifier()).isEqualTo(2);
        assertThat(gorger.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void separateEnergyGainEventsGiveCumulativeBoosts() {
        Permanent gorger = addCreatureReady(player1, new TerritorialGorger());
        harness.setLibrary(player1, List.of(new LiveFast(), new LiveFast(), new LiveFast(), new LiveFast()));
        harness.setHand(player1, List.of(new LiveFast(), new LiveFast()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gorger.getPowerModifier()).isEqualTo(4);
        assertThat(gorger.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    void energyGainBoostsEachOwnGorgerButNotOpponentsGorger() {
        Permanent first = addCreatureReady(player1, new TerritorialGorger());
        Permanent second = addCreatureReady(player1, new TerritorialGorger());
        Permanent opponent = addCreatureReady(player2, new TerritorialGorger());
        harness.setLibrary(player1, List.of(new LiveFast(), new LiveFast()));
        harness.setHand(player1, List.of(new LiveFast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(2);
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    void boostedGorgerTramplesOverUnboostedGorger() {
        Permanent attacker = addCreatureReady(player1, new TerritorialGorger());
        Permanent blocker = addCreatureReady(player2, new TerritorialGorger());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new LiveFast(), new LiveFast()));
        harness.setHand(player1, List.of(new LiveFast()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 2));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }
}
