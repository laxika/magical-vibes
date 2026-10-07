package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PondProphet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunshowerDruid.class, PondProphet.class})
class SunshowerDruidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature and gains 1 life")
    void etbPutsCounterOnTargetCreatureAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PondProphet());
        harness.setHand(player1, List.of(new SunshowerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB does nothing if its target is removed before resolution")
    void etbDoesNothingIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PondProphet());
        harness.setHand(player1, List.of(new SunshowerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB can target the Druid itself on an otherwise empty battlefield")
    void etbCanTargetItself() {
        harness.setHand(player1, List.of(new SunshowerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent druid = findPermanent(player1, "Sunshower Druid");
        harness.handlePermanentChosen(player1, druid.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunshower Druid");
        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still resolves when the Druid leaves but its target remains")
    void etbResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PondProphet());
        harness.setHand(player1, List.of(new SunshowerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
