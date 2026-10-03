package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.j.JusticeStrike;
import com.github.laxika.magicalvibes.cards.m.ManiacalRage;
import com.github.laxika.magicalvibes.cards.s.SureStrike;
import com.github.laxika.magicalvibes.cards.t.TorchCourier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BargingSergeant.class, TorchCourier.class, SureStrike.class, JusticeStrike.class, ManiacalRage.class})
class BargingSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new BargingSergeant());
        Permanent attackingCourier = addCreatureReady(player1, new TorchCourier());
        Permanent nonAttackingCourier = addCreatureReady(player1, new TorchCourier());
        Permanent equalPowerCreature = addCreatureReady(player1, new BargingSergeant());

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingCourier.getId());

        harness.handlePermanentChosen(player1, attackingCourier.getId());
        resolveAllTriggers();

        assertThat(attackingCourier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingCourier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Haste allows Barging Sergeant to attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.setHand(player1, List.of(new BargingSergeant()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Mentor does not put a counter on a target that reaches equal power in response")
    void mentorRechecksTargetPowerOnResolution() {
        addCreatureReady(player1, new BargingSergeant());
        Permanent courier = addCreatureReady(player1, new TorchCourier());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, courier.getId());
            harness.castAndResolveInstant(player1, 0, courier.getId());
            assertThat(gqs.getEffectivePower(gd, courier)).isEqualTo(4);
            resolveAllTriggers();
        });

        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor uses the source's last power including counters after it dies")
    void mentorUsesLastKnownPowerWithCounters() {
        Permanent source = addCreatureReady(player1, new BargingSergeant());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player1, new BargingSergeant());
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, target.getId());
            harness.castAndResolveInstant(player1, 0, source.getId());
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
            resolveAllTriggers();
        });

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor uses the source's last power including an Aura boost after it dies")
    void mentorUsesLastKnownPowerWithAuraBoost() {
        Permanent source = addCreatureReady(player1, new BargingSergeant());
        Permanent target = addCreatureReady(player1, new BargingSergeant());
        harness.setHand(player1, List.of(new ManiacalRage(), new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, source.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, target.getId());
            harness.castAndResolveInstant(player1, 0, source.getId());
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
            harness.assertInGraveyard(player1, "Maniacal Rage");
            resolveAllTriggers();
        });

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
