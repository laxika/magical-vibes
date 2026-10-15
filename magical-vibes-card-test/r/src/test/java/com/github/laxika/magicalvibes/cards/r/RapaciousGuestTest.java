package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({RapaciousGuest.class, Food.class, GrizzlyBears.class, Murder.class})
class RapaciousGuestTest extends BaseCardTest {

    @Test
    @DisplayName("One or more creatures dealing combat damage creates one Food")
    void combatDamageCreatesOneFood() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RapaciousGuest());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Sacrificing a Food puts a +1/+1 counter on Rapacious Guest")
    void sacrificingFoodPutsCounterOnGuest() {
        Permanent guest = addCreatureReady(player1, new RapaciousGuest());
        Food foodCard = new Food();
        foodCard.setName("Food");
        Permanent food = harness.addToBattlefieldAndReturn(player1, foodCard);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, food), 0, null, null);
        harness.passBothPriorities();

        assertThat(guest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Leaving the battlefield makes a target opponent lose the Guest's power")
    void leavingBattlefieldUsesLastKnownPower() {
        Permanent guest = addCreatureReady(player1, new RapaciousGuest());
        guest.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, guest.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void otherCreatureCombatDamageTriggersNonattackingGuest() {
        harness.addToBattlefield(player1, new RapaciousGuest());
        addCreatureReady(player1, new RapaciousGuest());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    void opponentSacrificingFoodDoesNotPutCounterOnGuest() {
        Permanent guest = addCreatureReady(player1, new RapaciousGuest());
        Permanent food = harness.addToBattlefieldAndReturn(player2, new Food());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, battlefieldIndex(player2, food), 0, null, null);
        resolveAllTriggers();

        assertThat(guest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player2, "Food")).isZero();
    }

    @Test
    void eachFoodSacrificePutsCounterOnEveryGuest() {
        Permanent first = addCreatureReady(player1, new RapaciousGuest());
        Permanent second = addCreatureReady(player1, new RapaciousGuest());
        Permanent firstFood = harness.addToBattlefieldAndReturn(player1, new Food());
        Permanent secondFood = harness.addToBattlefieldAndReturn(player1, new Food());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(player1, firstFood), 0, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, battlefieldIndex(player1, secondFood), 0, null, null);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void destroyingFoodDoesNotCountAsSacrificingIt() {
        Permanent guest = addCreatureReady(player1, new RapaciousGuest());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new Food());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, food));
        resolveAllTriggers();

        assertThat(guest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void returningGuestToHandUsesLastKnownPower() {
        Permanent guest = addCreatureReady(player1, new RapaciousGuest());
        guest.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, guest));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Rapacious Guest");
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
