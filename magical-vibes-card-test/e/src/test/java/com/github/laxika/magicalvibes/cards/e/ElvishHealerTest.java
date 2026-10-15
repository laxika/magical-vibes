package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArcticFoxes;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.n.NissaWorldwaker;
import com.github.laxika.magicalvibes.cards.w.WoollySpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishHealer.class, ArcticFoxes.class, Incinerate.class, NissaWorldwaker.class,
        WoollySpider.class})
class ElvishHealerTest extends BaseCardTest {

    private void addHealerReady() {
        addCreatureReady(player1, new ElvishHealer());
    }

    private void castIncinerateAt(UUID targetId) {
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Prevents 1 damage to a non-green creature")
    void preventsOneOnNonGreenCreature() {
        addHealerReady();
        Permanent foxes = addCreatureReady(player2, new ArcticFoxes());

        harness.activateAbility(player1, 0, null, foxes.getId());
        harness.passBothPriorities();

        assertThat(foxes.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents 2 damage to a green creature")
    void preventsTwoOnGreenCreature() {
        addHealerReady();
        Permanent spider = addCreatureReady(player2, new WoollySpider());

        harness.activateAbility(player1, 0, null, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents 1 damage to a green planeswalker")
    void preventsOneOnGreenPlaneswalker() {
        addHealerReady();
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaWorldwaker());
        nissa.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, nissa.getId());
        harness.passBothPriorities();

        assertThat(nissa.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents 1 damage to a player")
    void preventsOneOnPlayer() {
        addHealerReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents only the next 1 damage to a player")
    void preventsOnlyNextOneDamageToPlayer() {
        addHealerReady();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        castIncinerateAt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Prevents 2 of the next 3 damage to a green creature")
    void preventsTwoOfNextThreeDamageToGreenCreature() {
        addHealerReady();
        Permanent spider = addCreatureReady(player2, new WoollySpider());

        harness.activateAbility(player1, 0, null, spider.getId());
        harness.passBothPriorities();
        castIncinerateAt(spider.getId());

        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        assertThat(spider.getDamagePreventionShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider);
    }

    @Test
    void activationTapsHealerAndCannotBeRepeatedWhileTapped() {
        Permanent healer = addCreatureReady(player1, new ElvishHealer());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(healer.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void summoningSickHealerCannotActivate() {
        harness.addToBattlefield(player1, new ElvishHealer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void greenPlaneswalkerPreventsOnlyOneDamageAndConsumesShield() {
        addHealerReady();
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaWorldwaker());
        nissa.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, nissa.getId());
        harness.passBothPriorities();
        castIncinerateAt(nissa.getId());

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(nissa.getDamagePreventionShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nissa);
    }

    @Test
    void separateHealersAccumulatePreventionAcrossDamageEvents() {
        addHealerReady();
        addHealerReady();
        Permanent spider = addCreatureReady(player2, new WoollySpider());

        harness.activateAbility(player1, 0, null, spider.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, spider.getId());
        harness.passBothPriorities();
        castIncinerateAt(spider.getId());

        assertThat(spider.getMarkedDamage()).isZero();
        assertThat(spider.getDamagePreventionShield()).isEqualTo(1);

        castIncinerateAt(spider.getId());

        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        assertThat(spider.getDamagePreventionShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider);
    }

    @Test
    void preventionStillResolvesAfterHealerDies() {
        Permanent healer = addCreatureReady(player1, new ElvishHealer());
        Permanent spider = addCreatureReady(player2, new WoollySpider());

        harness.activateAbility(player1, 0, null, spider.getId());
        castIncinerateAt(healer.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(healer);
        harness.passBothPriorities();
        castIncinerateAt(spider.getId());

        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        assertThat(spider.getDamagePreventionShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider);
    }

    @Test
    void unusedPlayerAndCreatureShieldsExpireAtEndOfTurn() {
        addHealerReady();
        addHealerReady();
        Permanent spider = addCreatureReady(player2, new WoollySpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, spider.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(spider.getDamagePreventionShield()).isZero();
        castIncinerateAt(player2.getId());
        harness.assertLife(player2, 17);
    }
}
