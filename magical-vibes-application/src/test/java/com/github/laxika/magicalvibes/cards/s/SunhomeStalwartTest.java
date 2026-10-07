package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HuntedWitness;
import com.github.laxika.magicalvibes.cards.j.JusticeStrike;
import com.github.laxika.magicalvibes.cards.j.JusticiarsPortal;
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

@CardUsed({SunhomeStalwart.class, HuntedWitness.class, VernadiShieldmate.class,
        JusticeStrike.class, JusticiarsPortal.class})
class SunhomeStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new SunhomeStalwart());
        Permanent attackingWizard = addCreatureReady(player1, new HuntedWitness());
        Permanent nonAttackingWizard = addCreatureReady(player1, new HuntedWitness());
        Permanent equalPowerCreature = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(0, 1, 3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingWizard.getId());

        harness.handlePermanentChosen(player1, attackingWizard.getId());
        resolveAllTriggers();

        assertThat(attackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor uses the source's last known power if it leaves before resolution")
    void mentorUsesSourceLastKnownPower() {
        Permanent stalwart = addCreatureReady(player1, new SunhomeStalwart());
        Permanent wizard = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, wizard.getId());
        gd.playerBattlefields.get(player1.getId()).remove(stalwart);
        resolveAllTriggers();

        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor cannot target itself or an equal-power attacker")
    void mentorHasNoLegalTargetAmongEqualPowerAttackers() {
        Permanent stalwart = addCreatureReady(player1, new SunhomeStalwart());
        Permanent shieldmate = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(stalwart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(shieldmate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor does not add a counter when its target grows to equal power")
    void mentorRechecksTargetPower() {
        addCreatureReady(player1, new SunhomeStalwart());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        witness.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor compares against the source's current power on resolution")
    void mentorRechecksSourcePower() {
        Permanent stalwart = addCreatureReady(player1, new SunhomeStalwart());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        stalwart.setPowerModifier(-1);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor does not add a counter to a creature that stopped attacking")
    void mentorRechecksAttackingStatus() {
        addCreatureReady(player1, new SunhomeStalwart());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        witness.setAttacking(false);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor uses power at death even if the source grew after triggering")
    void mentorUsesPowerAtDeathRatherThanPowerAtTrigger() {
        Permanent stalwart = addCreatureReady(player1, new SunhomeStalwart());
        Permanent witness = addCreatureReady(player1, new HuntedWitness());
        harness.setHand(player1, List.of(new JusticeStrike()));

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, witness.getId());
        stalwart.setPowerModifier(1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, stalwart.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stalwart);
        witness.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mentor uses the original source's last-known power after it leaves and returns")
    void mentorUsesOriginalSourcePowerAfterFlicker() {
        Permanent stalwart = addCreatureReady(player1, new SunhomeStalwart());
        Permanent shieldmate = addCreatureReady(player1, new VernadiShieldmate());
        stalwart.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new JusticiarsPortal()));

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, shieldmate.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, stalwart.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stalwart);
        resolveAllTriggers();

        assertThat(shieldmate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First strike kills a blocking creature before it damages Sunhome Stalwart")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent stalwart = addCreatureReady(player1, new SunhomeStalwart());
        Permanent blocker = addCreatureReady(player2, new VernadiShieldmate());
        stalwart.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stalwart);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("An unblocked Sunhome Stalwart deals combat damage only once")
    void firstStrikeDoesNotDealRegularDamageAgain() {
        Permanent stalwart = addCreatureReady(player1, new SunhomeStalwart());
        stalwart.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
