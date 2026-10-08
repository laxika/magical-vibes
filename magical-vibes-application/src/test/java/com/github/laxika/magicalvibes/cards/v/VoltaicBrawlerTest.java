package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoltaicBrawler.class})
class VoltaicBrawlerTest extends BaseCardTest {

    @Test
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new VoltaicBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void mayPayEnergyOnAttackForBoostAndTrample() {
        Permanent brawler = addCreatureReady(player1, new VoltaicBrawler());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void decliningEnergyPaymentDoesNothing() {
        Permanent brawler = addCreatureReady(player1, new VoltaicBrawler());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotGetBoostWithoutEnergy() {
        Permanent brawler = addCreatureReady(player1, new VoltaicBrawler());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void enteringAddsEnergyOnlyToItsController() {
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerEnergyCounters.put(player2.getId(), 4);
        harness.enterBattlefieldAndReturn(player2, new VoltaicBrawler());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(6);
    }

    @Test
    void payingWithSurplusEnergySpendsExactlyOneAndBoostsOnlyTheAttacker() {
        Permanent brawler = addCreatureReady(player2, new VoltaicBrawler());
        Permanent other = addCreatureReady(player2, new VoltaicBrawler());
        gd.playerEnergyCounters.put(player1.getId(), 5);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void energyAvailabilityIsCheckedWhenTheAttackTriggerResolves() {
        Permanent brawler = addCreatureReady(player1, new VoltaicBrawler());

        declareAttackers(List.of(0));
        harness.enterBattlefieldAndReturn(player1, new VoltaicBrawler());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void boostAndTrampleExpireAtEndOfTurn() {
        Permanent brawler = addCreatureReady(player1, new VoltaicBrawler());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isFalse();
    }
}
