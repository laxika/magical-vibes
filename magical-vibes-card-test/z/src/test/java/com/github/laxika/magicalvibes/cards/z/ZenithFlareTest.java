package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GoForBlood;
import com.github.laxika.magicalvibes.cards.m.MemoryLeak;
import com.github.laxika.magicalvibes.cards.h.HumbleNaturalist;
import com.github.laxika.magicalvibes.cards.f.FlourishingFox;
import com.github.laxika.magicalvibes.cards.n.NarsetOfTheAncientWay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZenithFlare.class, GoForBlood.class, MemoryLeak.class, HumbleNaturalist.class,
        FlourishingFox.class, NarsetOfTheAncientWay.class})
class ZenithFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage and gains life equal to cycling cards in the controller's graveyard")
    void dealsDamageAndGainsLifeForCyclingCards() {
        harness.setGraveyard(player1, List.of(new GoForBlood(), new MemoryLeak(), new HumbleNaturalist()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Counts only the controller's cycling cards at resolution")
    void countsOnlyControllerCyclingCardsAtResolution() {
        harness.setGraveyard(player1, List.of(new GoForBlood(), new HumbleNaturalist()));
        harness.setGraveyard(player2, List.of(new MemoryLeak()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Resolves for zero when no cards in the graveyard have cycling")
    void noCyclingCardsMeansNoDamageOrLifeGain() {
        harness.setGraveyard(player1, List.of(new HumbleNaturalist()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Zenith Flare");
    }

    @Test
    @DisplayName("Includes a card cycled in response to Zenith Flare")
    void includesCardsCycledBeforeResolution() {
        harness.setGraveyard(player1, List.of(new GoForBlood()));
        harness.setHand(player1, List.of(new ZenithFlare(), new MemoryLeak()));
        harness.setLibrary(player1, List.of(new HumbleNaturalist()));
        addZenithFlareMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Memory Leak");
    }

    @Test
    @DisplayName("Uses the graveyard at resolution rather than the count when cast")
    void removedCyclingCardsDoNotCount() {
        harness.setGraveyard(player1, List.of(new GoForBlood(), new MemoryLeak()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Zenith Flare");
    }

    @Test
    @DisplayName("Can deal lethal damage to an opposing creature and still gains life")
    void damagesCreatureAndGainsLife() {
        var target = harness.addToBattlefieldAndReturn(player2, new HumbleNaturalist());
        harness.setGraveyard(player1, List.of(new GoForBlood(), new MemoryLeak(), new FlourishingFox()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Humble Naturalist");
        harness.assertInGraveyard(player2, "Humble Naturalist");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("An own cycling creature killed by Zenith Flare does not increase its life gain")
    void cyclingCreatureDiesOnlyAfterLifeGain() {
        var target = harness.addToBattlefieldAndReturn(player1, new FlourishingFox());
        harness.setGraveyard(player1, List.of(new GoForBlood()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flourishing Fox");
        harness.assertInGraveyard(player1, "Flourishing Fox");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life if its only target has left the battlefield")
    void illegalTargetPreventsLifeGain() {
        var target = harness.addToBattlefieldAndReturn(player2, new HumbleNaturalist());
        harness.setGraveyard(player1, List.of(new GoForBlood(), new MemoryLeak()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setHand(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Zenith Flare");
    }

    @Test
    @DisplayName("Can target its controller and gain life before checking for a loss")
    void selfTargetSurvivesTemporaryZeroLife() {
        harness.setLife(player1, 1);
        harness.setGraveyard(player1, List.of(new GoForBlood(), new MemoryLeak()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Can target a planeswalker and gains life independently of loyalty lost")
    void damagesPlaneswalkerAndGainsLife() {
        var target = harness.addToBattlefieldAndReturn(player2, new NarsetOfTheAncientWay());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setGraveyard(player1, List.of(new GoForBlood(), new MemoryLeak()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Narset of the Ancient Way");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Preventing damage does not reduce Zenith Flare's life gain")
    void preventedDamageStillGainsFullLife() {
        var target = harness.addToBattlefieldAndReturn(player2, new HumbleNaturalist());
        target.setDamagePreventionShield(2);
        harness.setGraveyard(player1, List.of(new GoForBlood(), new MemoryLeak()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Humble Naturalist");
        harness.assertLife(player1, 22);
    }

    private void addZenithFlareMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
