package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SpiderMobile.class, GiantSpider.class, GrizzlyBears.class})
class SpiderMobileTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Spider-Mobile +1/+1 for each Spider you control")
    void attackingScalesWithControlledSpiders() {
        Permanent mobile = addCreatureReady(player1, new SpiderMobile());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player1, new GiantSpider());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, mobile)).isTrue();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mobile)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mobile)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mobile)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocking gives Spider-Mobile +1/+1 for each Spider you control")
    void blockingScalesWithControlledSpiders() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent mobile = addCreatureReady(player2, new SpiderMobile());
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        addCreatureReady(player2, new GiantSpider());

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, spider.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, mobile)).isTrue();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                0, gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mobile)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mobile)).isEqualTo(5);
    }

    @Test
    @DisplayName("The attack boost counts your Spiders at resolution and stays fixed until cleanup")
    void attackBoostUsesResolutionCountAndExpires() {
        Permanent mobile = addCreatureReady(player1, new SpiderMobile());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);

        Permanent lateSpider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mobile)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mobile)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(lateSpider);
        assertThat(gqs.getEffectivePower(gd, mobile)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mobile)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, mobile)).isFalse();
        assertThat(mobile.getPowerModifier()).isZero();
        assertThat(mobile.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attacking without your own Spiders gives no boost")
    void opposingSpidersDoNotBoostMobile() {
        Permanent mobile = addCreatureReady(player1, new SpiderMobile());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mobile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mobile)).isEqualTo(3);
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew without tapping the Vehicle")
    void summoningSickSpiderCanCrew() {
        Permanent mobile = addCreatureReady(player1, new SpiderMobile());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        spider.setSummoningSick(true);
        addCreatureReady(player1, new GiantSpider());

        assertThat(gqs.isCreature(gd, mobile)).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spider.getId());
        assertThat(spider.isTapped()).isTrue();
        assertThat(mobile.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, mobile)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mobile)).isTrue();
        assertThat(mobile.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, mobile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mobile)).isEqualTo(3);
    }

    @Test
    @DisplayName("The attack boost contributes to excess trample damage")
    void boostedMobileTramplesOverBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SpiderMobile());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player1, new GiantSpider());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Spider-Mobile");
    }
}
