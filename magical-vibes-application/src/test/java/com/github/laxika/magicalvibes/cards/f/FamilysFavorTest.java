package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PortTown;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FamilysFavor.class, SakuraTribeElder.class, PortTown.class})
class FamilysFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, it targets an attacking creature and gives it a shield counter")
    void targetsAttackingCreatureAndAddsShieldCounter() {
        harness.addToBattlefieldAndReturn(player1, new FamilysFavor());
        Permanent attacker = addCreatureReady(player1, new SakuraTribeElder());
        Permanent nonAttacker = addCreatureReady(player1, new SakuraTribeElder());

        declareAttackers(player1, List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAttackTrigger();

        assertThat(attacker.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    @DisplayName("A shielded attacker draws when its combat damage removes the shield counter")
    void drawsWhenCombatDamageRemovesShieldCounter() {
        harness.addToBattlefieldAndReturn(player1, new FamilysFavor());
        Permanent attacker = addCreatureReady(player1, new SakuraTribeElder());
        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAttackTrigger();

        harness.setLibrary(player1, List.of(new PortTown()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("The granted combat-damage trigger expires at end of turn")
    void grantedTriggerExpiresAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new FamilysFavor());
        Permanent attacker = addCreatureReady(player1, new SakuraTribeElder());
        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAttackTrigger();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setLibrary(player1, List.of(new PortTown()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(attacker.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void resolveAttackTrigger() {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
    }

    @Test
    @DisplayName("Removing the shield and drawing happen during the same resolution")
    void drawsDuringCounterRemovalResolution() {
        harness.addToBattlefield(player1, new FamilysFavor());
        Permanent attacker = addCreatureReady(player1, new SakuraTribeElder());
        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAttackTrigger();
        harness.setLibrary(player1, List.of(new PortTown()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            resolveCombat();
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(attacker.getCounterCount(CounterType.SHIELD)).isZero();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("No card is drawn if the shield is gone when combat damage triggers")
    void doesNotDrawWithoutShieldCounter() {
        harness.addToBattlefield(player1, new FamilysFavor());
        Permanent attacker = addCreatureReady(player1, new SakuraTribeElder());
        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAttackTrigger();
        attacker.setCounterCount(CounterType.SHIELD, 0);
        harness.setLibrary(player1, List.of(new PortTown()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Attacking with multiple creatures produces one shield on the chosen attacker")
    void triggersOnceForMultipleAttackers() {
        harness.addToBattlefield(player1, new FamilysFavor());
        Permanent first = addCreatureReady(player1, new SakuraTribeElder());
        Permanent second = addCreatureReady(player1, new SakuraTribeElder());
        declareAttackers(List.of(1, 2));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, second.getId());
        resolveAttackTrigger();

        assertThat(first.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(second.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
