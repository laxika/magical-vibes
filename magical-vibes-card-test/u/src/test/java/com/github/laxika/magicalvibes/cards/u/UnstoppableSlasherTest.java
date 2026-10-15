package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AllWillBeOne;
import com.github.laxika.magicalvibes.cards.b.BalemurkLeech;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnstoppableSlasher.class, BalemurkLeech.class, AllWillBeOne.class})
class UnstoppableSlasherTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes a player lose half their life, rounded up")
    void combatDamageMakesPlayerLoseHalfLifeRoundedUp() {
        harness.setLife(player2, 23);
        Permanent slasher = addCreatureReady(player1, new UnstoppableSlasher());
        slasher.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Does not trigger when blocked and no combat damage reaches the player")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        harness.setLife(player2, 22);
        Permanent slasher = addCreatureReady(player1, new UnstoppableSlasher());
        slasher.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalemurkLeech());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Returns from its first death tapped with two stun counters")
    void returnsFromFirstDeathTappedWithStunCounters() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new UnstoppableSlasher());
        slasher.setMarkedDamage(slasher.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Unstoppable Slasher");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Unstoppable Slasher");
    }

    @Test
    @DisplayName("Stays in the graveyard when it dies with any counter")
    void staysInGraveyardWhenItDiesWithACounter() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new UnstoppableSlasher());
        slasher.setCounterCount(CounterType.STUN, 1);
        slasher.setMarkedDamage(slasher.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Unstoppable Slasher");
        harness.assertInGraveyard(player1, "Unstoppable Slasher");
    }

    @Test
    @DisplayName("Life loss uses the damaged player's life total at resolution")
    void usesLifeTotalAtResolution() {
        harness.setLife(player2, 22);
        Permanent slasher = addCreatureReady(player1, new UnstoppableSlasher());
        slasher.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        harness.setLife(player2, 17);
        harness.passBothPriorities();

        harness.assertLife(player2, 8);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An even life total is halved after combat damage")
    void halvesEvenLifeTotalAfterCombatDamage() {
        harness.setLife(player2, 22);
        Permanent slasher = addCreatureReady(player1, new UnstoppableSlasher());
        slasher.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("A positive power/toughness counter prevents the death ability from triggering")
    void doesNotTriggerWithPlusOneCounter() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new UnstoppableSlasher());
        slasher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        slasher.setMarkedDamage(slasher.getEffectiveToughness());

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Unstoppable Slasher");
        harness.assertInGraveyard(player1, "Unstoppable Slasher");
    }

    @Test
    @DisplayName("A redundant deathtouch counter still prevents the death ability")
    void doesNotTriggerWithKeywordCounter() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new UnstoppableSlasher());
        slasher.setCounterCount(CounterType.DEATHTOUCH, 1);
        slasher.setMarkedDamage(slasher.getEffectiveToughness());

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Unstoppable Slasher");
        harness.assertNotOnBattlefield(player1, "Unstoppable Slasher");
    }

    @Test
    @DisplayName("A stolen Slasher returns under its owner's control")
    void returnsUnderOwnersControl() {
        UnstoppableSlasher card = new UnstoppableSlasher();
        card.setOwnerId(player1.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, card);
        stolen.setMarkedDamage(stolen.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Unstoppable Slasher");
        assertThat(returned.getCard().getId()).isEqualTo(card.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Unstoppable Slasher");
        harness.assertNotInGraveyard(player1, "Unstoppable Slasher");
        harness.assertNotInGraveyard(player2, "Unstoppable Slasher");
    }

    @Test
    @DisplayName("Only the dying Slasher returns, not another copy in the graveyard")
    void returnsOnlyItsOwnCard() {
        UnstoppableSlasher other = new UnstoppableSlasher();
        harness.setGraveyard(player1, java.util.List.of(other));
        UnstoppableSlasher card = new UnstoppableSlasher();
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, card);
        slasher.setMarkedDamage(slasher.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Unstoppable Slasher")).hasSize(1);
        assertThat(findPermanent(player1, "Unstoppable Slasher").getCard().getId()).isEqualTo(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("After both stun counters are consumed, Slasher can return from another death")
    void returnsAgainAfterStunCountersAreConsumed() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new UnstoppableSlasher());
        slasher.setMarkedDamage(slasher.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Unstoppable Slasher");

        harness.performUntapStep(player1);
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player1);
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player1);
        assertThat(returned.isTapped()).isFalse();

        returned.setMarkedDamage(returned.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returnedAgain = findPermanent(player1, "Unstoppable Slasher");
        assertThat(returnedAgain.getId()).isNotEqualTo(returned.getId());
        assertThat(returnedAgain.isTapped()).isTrue();
        assertThat(returnedAgain.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Unstoppable Slasher");
    }

    @Test
    @CardUsed({UnstoppableSlasher.class, AllWillBeOne.class})
    @DisplayName("Returning with stun counters triggers All Will Be One for two damage")
    void returningWithStunCountersTriggersCounterPlacementAbilities() {
        harness.addToBattlefield(player1, new AllWillBeOne());
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new UnstoppableSlasher());
        slasher.setMarkedDamage(slasher.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Unstoppable Slasher").getCounterCount(CounterType.STUN))
                .isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
