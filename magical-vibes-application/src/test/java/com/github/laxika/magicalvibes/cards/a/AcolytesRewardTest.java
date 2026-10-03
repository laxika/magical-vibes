package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.b.BelenonWarAnthem;
import com.github.laxika.magicalvibes.cards.i.InvasionOfBelenon;
import com.github.laxika.magicalvibes.cards.k.KioraTheCrashingWave;
import com.github.laxika.magicalvibes.cards.s.SatyrFiredancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcolytesReward.class, GrizzlyBears.class, HillGiant.class, SuntailHawk.class,
        Shock.class, SatyrFiredancer.class, KioraTheCrashingWave.class,
        InvasionOfBelenon.class, BelenonWarAnthem.class})
class AcolytesRewardTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage to the first target and deals it to the second target player")
    void preventsDamageToTargetCreatureAndRedirectsToPlayer() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addWhiteDevotion(2);
        castReward(protectedCreature, player2.getId());

        Permanent attacker = addAttacker(new GrizzlyBears());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Devotion determines the prevention amount and excess damage is dealt normally")
    void devotionAmountLeavesExcessDamage() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addWhiteDevotion(2);
        castReward(protectedCreature, player2.getId());

        Permanent attacker = addAttacker(new HillGiant());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The second target can be a creature")
    void redirectsToTargetCreature() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent redirectCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addWhiteDevotion(2);
        castReward(protectedCreature, redirectCreature.getId());

        Permanent attacker = addAttacker(new HillGiant());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The prevention shield expires at end of turn")
    void shieldExpiresAtEndOfTurn() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addWhiteDevotion(2);
        castReward(protectedCreature, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent attacker = addAttacker(new GrizzlyBears());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Zero white devotion prevents no damage")
    void zeroDevotionCreatesNoPrevention() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        castReward(protectedCreature, player2.getId());

        Permanent attacker = addAttacker(new GrizzlyBears());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An illegal second target does not stop prevention")
    void preventsDamageWhenSecondTargetLeavesBeforeResolution() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addWhiteDevotion(2);
        harness.setHand(player1, List.of(new AcolytesReward()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(protectedCreature.getId(), recipient.getId()));
        shock(recipient.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");

        Permanent attacker = addAttacker(new GrizzlyBears());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An illegal first target does not cause damage to the second target")
    void noDamageWhenProtectedCreatureLeavesBeforeResolution() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addWhiteDevotion(2);
        harness.setHand(player1, List.of(new AcolytesReward()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(protectedCreature.getId(), player2.getId()));
        shock(protectedCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Losing the damage recipient after resolution does not remove prevention")
    void preventsDamageAfterRecipientLeaves() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addWhiteDevotion(2);
        castReward(protectedCreature, recipient.getId());
        shock(recipient.getId());

        Permanent attacker = addAttacker(new GrizzlyBears());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Devotion is evaluated at resolution and stays fixed afterward")
    void devotionIsFixedAtResolution() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addWhiteDevotion(1);
        harness.setHand(player1, List.of(new AcolytesReward()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(protectedCreature.getId(), player2.getId()));
        addWhiteDevotion(1);
        harness.passBothPriorities();
        addWhiteDevotion(3);

        Permanent attacker = addAttacker(new HillGiant());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The shield spans separate noncombat damage events and is consumed")
    void shieldSpansDamageEvents() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addWhiteDevotion(3);
        castReward(protectedCreature, player2.getId());

        shock(protectedCreature.getId());
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 18);
        shock(protectedCreature.getId());
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 17);
        shock(protectedCreature.getId());
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The first target may be an opponent's creature and the second may be you")
    void protectsOpposingCreatureAndDamagesController() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addWhiteDevotion(2);
        castReward(protectedCreature, player1.getId());
        shock(protectedCreature.getId());

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The same creature can be chosen for both targets")
    void sameCreatureCanBeBothTargets() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addWhiteDevotion(2);
        castReward(protectedCreature, protectedCreature.getId());
        shock(protectedCreature.getId());

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevented damage can be dealt to a planeswalker")
    void damagesPlaneswalker() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new KioraTheCrashingWave());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        addWhiteDevotion(2);
        castReward(protectedCreature, harness.getPermanentId(player2, "Kiora, the Crashing Wave"));
        shock(protectedCreature.getId());

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Kiora, the Crashing Wave");
    }

    @Test
    @DisplayName("Any target includes battles")
    void damagesBattle() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfBelenon());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        addWhiteDevotion(2);
        castReward(protectedCreature, battle.getId());
        shock(protectedCreature.getId());

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Reward's damage is instant spell damage and triggers Satyr Firedancer")
    void preventedCombatDamageTriggersFiredancer() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new SatyrFiredancer());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addWhiteDevotion(2);
        castReward(protectedCreature, player2.getId());
        Permanent attacker = addAttacker(new GrizzlyBears());
        block(protectedCreature, attacker);
        runCombatDamage();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(recipient.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(recipient.getId()));
        assertThat(recipient.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 18);
    }

    private void shock(UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addWhiteDevotion(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new SuntailHawk());
        }
    }

    private void castReward(Permanent protectedCreature, UUID redirectTargetId) {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AcolytesReward()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(protectedCreature.getId(), redirectTargetId));
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        return attacker;
    }

    private void block(Permanent blocker, Permanent attacker) {
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player2.getId()).indexOf(attacker));
    }

    private void runCombatDamage() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passBothPriorities();
    }
}
