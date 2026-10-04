package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SureStrike;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HammerDropper.class, VernadiShieldmate.class, SureStrike.class})
class HammerDropperTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor puts a +1/+1 counter on a lesser-power attacking creature")
    void mentorCountersLesserPowerAttacker() {
        addCreatureReady(player1, new HammerDropper());
        Permanent attacker = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor excludes equal-power, greater-power, and nonattacking creatures")
    void mentorOnlyOffersEligibleAttackers() {
        Permanent hammerDropper = addCreatureReady(player1, new HammerDropper());
        Permanent attacker = addCreatureReady(player1, new VernadiShieldmate());
        Permanent equalPower = addCreatureReady(player1, new VernadiShieldmate());
        Permanent greaterPower = addCreatureReady(player1, new VernadiShieldmate());
        Permanent nonattacker = addCreatureReady(player1, new VernadiShieldmate());
        equalPower.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        greaterPower.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        declareAttackers(List.of(0, 1, 2, 3));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hammerDropper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(greaterPower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor does nothing when Hammer Dropper attacks alone")
    void mentorWithoutEligibleTarget() {
        Permanent hammerDropper = addCreatureReady(player1, new HammerDropper());
        Permanent nonattacker = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(hammerDropper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor does not trigger when Hammer Dropper does not attack")
    void mentorRequiresSourceToAttack() {
        Permanent hammerDropper = addCreatureReady(player1, new HammerDropper());
        Permanent attacker = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(hammerDropper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor rechecks lesser power when its target is boosted in response")
    void mentorTargetBecomesEqualPowerBeforeResolution() {
        addCreatureReady(player1, new HammerDropper());
        Permanent attacker = addCreatureReady(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor uses the source's current power when resolving")
    void mentorRechecksSourcePowerBeforeResolution() {
        Permanent hammerDropper = addCreatureReady(player1, new HammerDropper());
        Permanent attacker = addCreatureReady(player1, new VernadiShieldmate());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        hammerDropper.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
