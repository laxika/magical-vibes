package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingRaptor.class, ChandraHopesBeacon.class, InvasionOfInnistrad.class})
class RampagingRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives Rampaging Raptor +2/+0 until end of turn")
    void activatedAbilityBoostsPower() {
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(6);
    }

    @Test
    @DisplayName("Combat damage to an opponent can target that opponent's planeswalker")
    void combatDamageTargetsOpponentsPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        raptor.setAttacking(true);
        raptor.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(planeswalker.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to an opponent can target a battle that opponent protects")
    void combatDamageTargetsBattleOpponentProtects() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        raptor.setAttacking(true);
        raptor.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(battle.getId());
        harness.handlePermanentChosen(player1, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple activations add together without increasing toughness")
    void activatedAbilityStacks() {
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(4);
    }

    @Test
    @DisplayName("Trigger uses combat damage dealt even if Raptor is pumped afterward")
    void triggerSnapshotsCombatDamage() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        raptor.setAttacking(true);
        raptor.setAttackTarget(player2.getId());
        harness.addMana(player1, ManaColor.RED, 3);

        resolveCombat();
        harness.handlePermanentChosen(player1, battle.getId());
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A battle protected by the Raptor's controller is not a legal target")
    void excludesBattleProtectedByController() {
        Permanent legalBattle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        legalBattle.setProtectorPlayerId(player2.getId());
        legalBattle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent illegalBattle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        illegalBattle.setProtectorPlayerId(player1.getId());
        illegalBattle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent ownPlaneswalker = harness.addToBattlefieldAndReturn(player1, new ChandraHopesBeacon());
        ownPlaneswalker.setCounterCount(CounterType.LOYALTY, 6);
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        raptor.setAttacking(true);
        raptor.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(legalBattle.getId());
        harness.handlePermanentChosen(player1, legalBattle.getId());
        harness.passBothPriorities();

        assertThat(illegalBattle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("Changing a battle's protector makes the trigger's target illegal")
    void rechecksBattleProtectorAtResolution() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        raptor.setAttacking(true);
        raptor.setAttackTarget(player2.getId());

        resolveCombat();
        harness.handlePermanentChosen(player1, battle.getId());
        battle.setProtectorPlayerId(player1.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
    }

    @Test
    @DisplayName("The power boost expires at end of turn")
    void powerBoostExpires() {
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(6);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(4);
    }

    @Test
    @DisplayName("Combat damage still damages the opponent when there is no legal trigger target")
    void combatDamageWithoutLegalTarget() {
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        raptor.setAttacking(true);
        raptor.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a planeswalker does not trigger the additional damage ability")
    void combatDamageToPlaneswalkerDoesNotTrigger() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        raptor.setAttacking(true);
        raptor.setAttackTarget(planeswalker.getId());

        resolveCombat();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
