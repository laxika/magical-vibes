package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScatterToTheWinds.class, Forest.class, OranRiefInvoker.class})
class ScatterToTheWindsTest extends BaseCardTest {

    @Test
    void countersTargetSpellNormally() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        OranRiefInvoker invoker = new OranRiefInvoker();
        harness.setHand(player1, List.of(invoker));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ScatterToTheWinds()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, invoker.getId());

        harness.assertInGraveyard(player1, "Oran-Rief Invoker");
        harness.assertInGraveyard(player2, "Scatter to the Winds");
        harness.assertNotOnBattlefield(player1, "Oran-Rief Invoker");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void alternateCastAwakensTargetLand() {
        OranRiefInvoker invoker = new OranRiefInvoker();
        harness.setHand(player1, List.of(invoker));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player2, List.of(new ScatterToTheWinds()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        gs.playCardWithAlternateCost(gd, player2, 0, 0, null, null,
                List.of(invoker.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        harness.assertInGraveyard(player1, "Oran-Rief Invoker");
    }

    @Test
    void alternateCastRequiresLandTarget() {
        OranRiefInvoker invoker = new OranRiefInvoker();
        harness.setHand(player1, List.of(invoker));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ScatterToTheWinds()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player2, 0, 0, null, null,
                List.of(invoker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    @Test
    void awakensLandEvenWhenTargetSpellHasAlreadyBeenCountered() {
        OranRiefInvoker invoker = new OranRiefInvoker();
        harness.setHand(player1, List.of(invoker, new ScatterToTheWinds()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player2, List.of(new ScatterToTheWinds()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        gs.playCardWithAlternateCost(gd, player2, 0, 0, null, null,
                List.of(invoker.getId(), land.getId()));
        harness.castAndResolveInstant(player1, 0, invoker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Oran-Rief Invoker");
        harness.assertInGraveyard(player2, "Scatter to the Winds");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    @Test
    void countersSpellEvenWhenTargetLandHasLeftBattlefield() {
        OranRiefInvoker invoker = new OranRiefInvoker();
        harness.setHand(player1, List.of(invoker));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player2, List.of(new ScatterToTheWinds()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        gs.playCardWithAlternateCost(gd, player2, 0, 0, null, null,
                List.of(invoker.getId(), land.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Oran-Rief Invoker");
        harness.assertNotOnBattlefield(player1, "Oran-Rief Invoker");
        harness.assertInGraveyard(player2, "Scatter to the Winds");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.isPermanentlyAnimated()).isFalse();
    }

    @Test
    void awakeningSameLandTwiceAccumulatesCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        for (int cast = 0; cast < 2; cast++) {
            OranRiefInvoker invoker = new OranRiefInvoker();
            harness.setHand(player1, List.of(invoker));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.setHand(player2, List.of(new ScatterToTheWinds()));
            harness.addMana(player2, ManaColor.BLUE, 6);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            gs.playCardWithAlternateCost(gd, player2, 0, 0, null, null,
                    List.of(invoker.getId(), land.getId()));
            harness.passBothPriorities();
        }

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void alternateCastCannotTargetOpponentsLand() {
        OranRiefInvoker invoker = new OranRiefInvoker();
        harness.setHand(player1, List.of(invoker));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player2, List.of(new ScatterToTheWinds()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player2, 0, 0, null, null,
                List.of(invoker.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
