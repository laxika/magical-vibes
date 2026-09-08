package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BeastmasterAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers a quest counter for each attacking creature")
    void attackingOffersQuestCounter() {
        Permanent ascension = addAscension();
        Permanent attacker = addReadyCreature(player1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the attack trigger adds no quest counter")
    void decliningAttackTriggerAddsNoCounter() {
        Permanent ascension = addAscension();
        addReadyCreature(player1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Seven quest counters give creatures you control +5/+5")
    void sevenQuestCountersBoostOwnCreatures() {
        Permanent ascension = addAscension();
        Permanent attacker = addReadyCreature(player1);
        Permanent idle = addReadyCreature(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        ascension.setCounterCount(CounterType.QUEST, 7);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, idle)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The seventh accepted quest counter turns on the creature boost")
    void seventhQuestCounterTurnsOnBoost() {
        Permanent ascension = addAscension();
        Permanent attacker = addReadyCreature(player1);
        ascension.setCounterCount(CounterType.QUEST, 6);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(7);
    }

    private Permanent addAscension() {
        return harness.addToBattlefieldAndReturn(player1, new BeastmasterAscension());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
