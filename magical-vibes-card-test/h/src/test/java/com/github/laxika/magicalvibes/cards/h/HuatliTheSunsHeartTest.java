package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BelligerentBrontodon;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuatliTheSunsHeart.class, GoblinPiker.class, GiantSpider.class, BelligerentBrontodon.class})
class HuatliTheSunsHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures assign combat damage using toughness")
    void ownCreaturesUseToughnessForCombatDamage() {
        addReadyHuatli(player1, 3);
        Permanent ownPiker = addReadyCreature(player1, new GoblinPiker());
        Permanent opponentPiker = addReadyCreature(player2, new GoblinPiker());

        assertThat(gqs.getEffectiveCombatDamage(gd, ownPiker)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentPiker)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 gains life equal to the greatest toughness among your creatures")
    void minusThreeGainsGreatestControlledToughness() {
        Permanent huatli = addReadyHuatli(player1, 3);
        addReadyCreature(player1, new GoblinPiker());
        addReadyCreature(player1, new GiantSpider());
        addReadyCreature(player2, new BelligerentBrontodon());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    void combatUsesModifiedToughnessWithoutSubtractingMarkedDamage() {
        addReadyHuatli(player1, 7);
        Permanent spider = addReadyCreature(player1, new GiantSpider());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        spider.setMarkedDamage(3);
        spider.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 14);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
    }

    @Test
    void spendingLastLoyaltyStopsStaticEffectButLifeAbilityStillResolves() {
        addReadyHuatli(player1, 3);
        Permanent piker = addReadyCreature(player1, new GoblinPiker());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Huatli, the Sun's Heart");
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void minusThreeGainsNoLifeWithoutControlledCreatures() {
        addReadyHuatli(player1, 7);
        addReadyCreature(player2, new GiantSpider());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void minusThreeUsesCurrentToughnessAtResolution() {
        addReadyHuatli(player1, 7);
        Permanent spider = addReadyCreature(player1, new GiantSpider());
        spider.setMarkedDamage(2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
    }

    private Permanent addReadyHuatli(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HuatliTheSunsHeart());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
