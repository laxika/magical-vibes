package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CinderPyromancer;
import com.github.laxika.magicalvibes.cards.m.MerrowLevitator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiverfallMimic.class, MerrowLevitator.class, CinderPyromancer.class})
class RiverfallMimicTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting a blue-and-red spell makes the Mimic 3/3 and unblockable")
    void blueRedSpellPumpsMimic() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());
        assertThat(mimic.isCantBeBlocked()).isFalse();

        harness.setHand(player1, List.of(new RiverfallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.isCantBeBlocked()).isFalse();
        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(mimic.getEffectivePower()).isEqualTo(3);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(3);
        assertThat(mimic.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Pump and unblockable wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());

        harness.setHand(player1, List.of(new RiverfallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(3);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(mimic.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Casting a mono-blue spell does not trigger the Mimic")
    void monoBlueSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());

        harness.castFromHand(player1, new MerrowLevitator(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(mimic.isCantBeBlocked()).isFalse();
    }

    @Test
    void monoRedSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());

        harness.castFromHand(player1, new CinderPyromancer(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(mimic.isCantBeBlocked()).isFalse();
    }

    @Test
    void opponentsBlueRedSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RiverfallMimic()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(mimic.isCantBeBlocked()).isFalse();
    }

    @Test
    void hybridSpellPaidWithOnlyRedTriggersAndRepeatedTriggersDoNotAddPower() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());
        harness.setHand(player1, List.of(new RiverfallMimic(), new RiverfallMimic()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(mimic.getEffectivePower()).isEqualTo(3);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(3);
        assertThat(mimic.isCantBeBlocked()).isTrue();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(mimic.getEffectivePower()).isEqualTo(3);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(3);
        assertThat(mimic.isCantBeBlocked()).isTrue();
    }

    @Test
    void basePowerToughnessChangePreservesCounters() {
        Permanent mimic = addCreatureReady(player1, new RiverfallMimic());
        mimic.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new RiverfallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(5);
        assertThat(mimic.isCantBeBlocked()).isTrue();
        assertThat(mimic.getPlusOnePlusOneCounters()).isEqualTo(2);
    }
}
