package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.c.CanopyVista;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OranRiefHydra.class, Forest.class, Mountain.class, CanopyVista.class,
        BloodMoon.class, ScourFromExistence.class})
class OranRiefHydraTest extends BaseCardTest {

    @Test
    @DisplayName("A nonbasic Forest puts two counters on the Hydra")
    void nonbasicForestPutsTwoCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.setHand(player1, List.of(new CanopyVista()));

        harness.playLand(player1, 0);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lands entering without being played still trigger landfall")
    void landEnteringWithoutBeingPlayedTriggers() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());

        harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Hydra gets its own counters from a Forest")
    void eachHydraTriggersIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Forest leaving before resolution still gives two counters")
    void forestLeavingBeforeResolutionStillGivesTwoCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castInstant(player1, 0, forest.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An exiled nonbasic land uses its last known subtype under Blood Moon")
    void exiledLandUsesLastKnownSubtype() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.addToBattlefield(player1, new BloodMoon());
        harness.setHand(player1, List.of(new CanopyVista(), new ScourFromExistence()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Forest landfall puts two +1/+1 counters on Oran-Rief Hydra")
    void forestLandfallPutsTwoCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Forest landfall puts one +1/+1 counter on Oran-Rief Hydra")
    void nonForestLandfallPutsOneCounter() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Oran-Rief Hydra")
    void opponentLandDoesNotTrigger() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
