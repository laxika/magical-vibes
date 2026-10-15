package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DevouringStrossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWesternCloud.class, ChandraNalaar.class, DevouringStrossus.class,
        GrizzlyBears.class, Shock.class})
class TheWesternCloudTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheWesternCloud(), gd.nextTimestamp()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void preventsDamageToThePlaneswalkersAndCreaturesOfItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = addChandra(player1, 5);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DevouringStrossus());
        Permanent opponentPlaneswalker = addChandra(player2, 5);
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 8);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, planeswalker.getId());
        harness.castAndResolveInstant(player2, 0, opponentCreature.getId());
        harness.castAndResolveInstant(player2, 0, opponentPlaneswalker.getId());

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void chaosCreatesThreeTappedTreasuresThatEachDamageCreaturesAndPlaneswalkers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = addChandra(player1, 5);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DevouringStrossus());
        Permanent opponentPlaneswalker = addChandra(player2, 5);

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        assertThat(findPermanents(player1, "Treasure")).allMatch(Permanent::isTapped);
    }

    @Test
    void treasureDamageIsDealtDuringTheChaosAbilityResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevouringStrossus());
        Permanent planeswalker = addChandra(player2, 5);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotPreventDamageToPlayers() {
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player2.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void protectionFollowsTheCurrentPlanarController() {
        Permanent oldControllerCreature = harness.addToBattlefieldAndReturn(player1, new DevouringStrossus());
        Permanent newControllerCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent oldControllerPlaneswalker = addChandra(player1, 5);
        Permanent newControllerPlaneswalker = addChandra(player2, 5);
        gd.planechase.controllerId = player2.getId();
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castAndResolveInstant(player2, 0, oldControllerCreature.getId());
        harness.castAndResolveInstant(player2, 0, newControllerCreature.getId());
        harness.castAndResolveInstant(player2, 0, oldControllerPlaneswalker.getId());
        harness.castAndResolveInstant(player2, 0, newControllerPlaneswalker.getId());

        assertThat(oldControllerCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(newControllerCreature.getMarkedDamage()).isZero();
        assertThat(oldControllerPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(newControllerPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    private Permanent addChandra(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }
}
