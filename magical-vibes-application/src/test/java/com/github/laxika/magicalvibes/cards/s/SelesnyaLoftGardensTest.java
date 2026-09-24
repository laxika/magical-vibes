package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarReborn;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelesnyaLoftGardens.class, Forest.class, LlanowarReborn.class, RaiseTheAlarm.class})
class SelesnyaLoftGardensTest extends BaseCardTest {

    private PlanarObject addPlane() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        PlanarObject plane = new PlanarObject(new SelesnyaLoftGardens(), gd.nextTimestamp());
        gd.planechase.faceUp.add(plane);
        return plane;
    }

    @Test
    void doublesTokensAndCountersGlobally() {
        addPlane();
        Permanent playerOnePermanent = harness.enterBattlefieldAndReturn(player1, new LlanowarReborn());
        Permanent playerTwoPermanent = harness.enterBattlefieldAndReturn(player2, new LlanowarReborn());

        harness.setHand(player1, java.util.List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(4);
        assertThat(playerOnePermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(playerTwoPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void chaosDoublesManaFromYourLandUntilEndOfTurn() {
        addPlane();
        PlanechaseService planar = com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(PlanechaseService.class);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
