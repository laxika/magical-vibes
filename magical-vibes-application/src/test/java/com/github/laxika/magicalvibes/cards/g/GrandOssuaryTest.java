package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrandOssuary.class, GrizzlyBears.class, HillGiant.class, Panopticon.class, Assassinate.class})
class GrandOssuaryTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        gd.planechase.faceUp.add(new PlanarObject(new GrandOssuary(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void creatureDeathDistributesCountersEqualToItsPower() {
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        dying.tap();
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(firstTarget.getId(), 1, secondTarget.getId(), 2);

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void chaosExilesCreaturesAndCreatesSaprolingsForEachControllerByTotalPower() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p == ownBears);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p == opposingGiant);
        assertThat(gd.findExiledCard(ownBears.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(opposingGiant.getOriginalCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Saproling", "Saproling");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Saproling", "Saproling", "Saproling");
        assertThat(gd.planechase.faceUp).extracting(object -> object.getCard().getName())
                .containsExactly("Panopticon");
    }
}
