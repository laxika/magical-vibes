package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SatyrGrovedancer.class, GrizzlyBears.class, Forest.class, Hubris.class})
class SatyrGrovedancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a creature you control")
    void etbPutsCounterOnOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new SatyrGrovedancer(), "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB can target an opponent's creature")
    void etbCanTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new SatyrGrovedancer(), "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new SatyrGrovedancer(), "{1}{G}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Satyr Grovedancer"));
        harness.passBothPriorities();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can be cast onto an empty battlefield and target itself after entering")
    void canTargetItselfAfterEntering() {
        harness.castFromHand(player1, new SatyrGrovedancer(), "{1}{G}");
        harness.passBothPriorities();
        Permanent dancer = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handlePermanentChosen(player1, dancer.getId());
        harness.passBothPriorities();

        assertThat(dancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dancer.getEffectivePower()).isEqualTo(2);
        assertThat(dancer.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering without being cast still triggers the counter ability")
    void enteringWithoutCastingTriggersAbility() {
        Permanent dancer = harness.enterBattlefieldAndReturn(player1, new SatyrGrovedancer());
        harness.handlePermanentChosen(player1, dancer.getId());
        harness.passBothPriorities();

        assertThat(dancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A target returned to hand before resolution receives no counter")
    void removedTargetReceivesNoCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrGrovedancer());
        Permanent dancer = harness.enterBattlefieldAndReturn(player1, new SatyrGrovedancer());
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new Hubris()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Satyr Grovedancer");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(dancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves even when its source leaves the battlefield")
    void abilitySurvivesSourceLeaving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrGrovedancer());
        Permanent dancer = harness.enterBattlefieldAndReturn(player1, new SatyrGrovedancer());
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new Hubris()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, dancer.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Satyr Grovedancer");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
