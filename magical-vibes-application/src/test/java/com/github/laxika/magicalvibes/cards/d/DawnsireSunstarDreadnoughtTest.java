package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnsireSunstarDreadnought.class, GrizzlyBears.class})
class DawnsireSunstarDreadnoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent dreadnought = harness.addToBattlefieldAndReturn(player1,
                new DawnsireSunstarDreadnought());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(dreadnought), null, null);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(dreadnought.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Twenty charge counters make Dawnsire a flying artifact creature")
    void twentyCountersAnimateAndGrantFlying() {
        Permanent dreadnought = harness.addToBattlefieldAndReturn(player1,
                new DawnsireSunstarDreadnought());

        dreadnought.setCounterCount(CounterType.CHARGE, 19);
        assertThat(gqs.isCreature(gd, dreadnought)).isFalse();
        assertThat(gqs.hasKeyword(gd, dreadnought, Keyword.FLYING)).isFalse();

        dreadnought.setCounterCount(CounterType.CHARGE, 20);
        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dreadnought)).isEqualTo(20);
        assertThat(gqs.getEffectiveToughness(gd, dreadnought)).isEqualTo(20);
        assertThat(gqs.hasKeyword(gd, dreadnought, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("At ten charge counters, attacking deals 100 damage to a target creature")
    void tenCountersAttackTriggerDestroysCreature() {
        Permanent dreadnought = harness.addToBattlefieldAndReturn(player1,
                new DawnsireSunstarDreadnought());
        dreadnought.setCounterCount(CounterType.CHARGE, 10);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(battlefieldIndex(attacker)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The attack trigger does not exist below ten charge counters")
    void belowTenCountersDoesNotTrigger() {
        Permanent dreadnought = harness.addToBattlefieldAndReturn(player1,
                new DawnsireSunstarDreadnought());
        dreadnought.setCounterCount(CounterType.CHARGE, 9);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(battlefieldIndex(attacker)));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
