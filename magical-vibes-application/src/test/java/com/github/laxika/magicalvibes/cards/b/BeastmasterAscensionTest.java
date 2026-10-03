package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastmasterAscension.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("Each attacker generates an independently optional quest counter")
    void multipleAttackersOfferIndependentCounters() {
        Permanent ascension = addAscension();
        addReadyCreature(player1);
        addReadyCreature(player1);
        addReadyCreature(player1);

        declareAttackers(player1, List.of(1, 2, 3));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opposing attackers do not add quest counters")
    void opposingAttackDoesNotTrigger() {
        Permanent ascension = addAscension();
        addReadyCreature(player2);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost persists above seven quest counters and ends below seven")
    void boostTracksCurrentQuestCounterCount() {
        Permanent ascension = addAscension();
        Permanent creature = addReadyCreature(player1);
        ascension.setCounterCount(CounterType.QUEST, 8);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);

        ascension.setCounterCount(CounterType.QUEST, 6);
        ascension.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two active Ascensions each grant their own +5/+5 bonus")
    void activeAscensionsStackTheirBonuses() {
        Permanent first = addAscension();
        Permanent second = addAscension();
        Permanent creature = addReadyCreature(player1);
        first.setCounterCount(CounterType.QUEST, 7);
        second.setCounterCount(CounterType.QUEST, 7);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(12);
    }

    @Test
    @CardUsed({Opalescence.class})
    @DisplayName("An animated Ascension receives its own creature boost")
    void animatedAscensionReceivesItsOwnBoost() {
        Permanent ascension = addAscension();
        harness.addToBattlefield(player1, new Opalescence());
        ascension.setCounterCount(CounterType.QUEST, 7);

        assertThat(gqs.getEffectivePower(gd, ascension)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ascension)).isEqualTo(8);
    }

    private Permanent addAscension() {
        return harness.addToBattlefieldAndReturn(player1, new BeastmasterAscension());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
