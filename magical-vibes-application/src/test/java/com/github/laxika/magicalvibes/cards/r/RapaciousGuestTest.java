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

        harness.castInstant(player2, 0, guest.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
