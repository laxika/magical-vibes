package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarReborn;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelesnyaLoftGardens.class, Forest.class, LlanowarReborn.class, RaiseTheAlarm.class, SimicGrowthChamber.class})
class SelesnyaLoftGardensTest extends BaseCardTest {

    private void addPlane() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        PlanarObject plane = new PlanarObject(new SelesnyaLoftGardens(), gd.nextTimestamp());
        gd.planechase.faceUp.add(plane);
    }

    @Test
    void doublesTokensAndCountersGlobally() {
        addPlane();
        Permanent playerOnePermanent = harness.enterBattlefieldAndReturn(player1, new LlanowarReborn());
        Permanent playerTwoPermanent = harness.enterBattlefieldAndReturn(player2, new LlanowarReborn());

        harness.setHand(player1, java.util.List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Soldier")).hasSize(4);
        assertThat(playerOnePermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(playerTwoPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void chaosDoublesManaFromYourLandUntilEndOfTurn() {
        addPlane();
        resolveChaos();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    private void resolveChaos() {
        PlanechaseService planar = com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(PlanechaseService.class);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
    }

    @Test
    void doublesOpponentTokenCreation() {
        addPlane();
        harness.setHand(player2, java.util.List.of(new RaiseTheAlarm()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0);

        assertThat(findPermanents(player2, "Soldier")).hasSize(4);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    void chaosDoesNotBonusOpponentLands() {
        addPlane();
        resolveChaos();
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void repeatedChaosAddsOneManaPerResolvedAbility() {
        addPlane();
        resolveChaos();
        resolveChaos();
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void chaosBonusPersistsAfterPlaneLeaves() {
        addPlane();
        resolveChaos();
        gd.planechase.faceUp.clear();
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void chaosBonusExpiresAtCleanup() {
        addPlane();
        resolveChaos();
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.setLibrary(player2, java.util.List.of(new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void chaosAddsOnlyOneChosenManaFromMulticolorProduction() {
        addPlane();
        resolveChaos();
        harness.addToBattlefield(player1, new SimicGrowthChamber());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
