package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AnotherChance;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepGoblinSkulltaker.class, ZuranOrb.class, Forest.class, AnotherChance.class})
class DeepGoblinSkulltakerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself at your end step after descending")
    void putsCounterAfterDescending() {
        Permanent skulltaker = harness.addToBattlefieldAndReturn(player1, new DeepGoblinSkulltaker());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(skulltaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself at your end step without descending")
    void doesNotPutCounterWithoutDescending() {
        Permanent skulltaker = harness.addToBattlefieldAndReturn(player1, new DeepGoblinSkulltaker());

        advanceToEndStep(player1);

        assertThat(skulltaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void millingMultiplePermanentCardsStillAddsOnlyOneCounter() {
        Permanent skulltaker = harness.addToBattlefieldAndReturn(player1, new DeepGoblinSkulltaker());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new AnotherChance()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(skulltaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonpermanentCardsEnteringGraveyardDoNotCountAsDescending() {
        Permanent skulltaker = harness.addToBattlefieldAndReturn(player1, new DeepGoblinSkulltaker());
        harness.setLibrary(player1, List.of(new AnotherChance(), new AnotherChance()));
        harness.setHand(player1, List.of(new AnotherChance()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(skulltaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStepEvenAfterDescending() {
        Permanent skulltaker = harness.addToBattlefieldAndReturn(player1, new DeepGoblinSkulltaker());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(skulltaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void descendingAfterEndStepBeginsDoesNotTriggerRetroactively() {
        Permanent skulltaker = harness.addToBattlefieldAndReturn(player1, new DeepGoblinSkulltaker());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(skulltaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
