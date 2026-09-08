package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
