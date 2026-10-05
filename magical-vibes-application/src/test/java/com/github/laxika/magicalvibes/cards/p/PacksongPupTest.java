package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.cards.c.ChildOfThePack;
import com.github.laxika.magicalvibes.cards.s.SavagePackmate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PacksongPup.class, HowlpackWolf.class, Shock.class, ChildOfThePack.class, SavagePackmate.class})
class PacksongPupTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your combat, another Wolf puts a +1/+1 counter on Packsong Pup")
    void anotherWolfPutsCounterOnPup() {
        Permanent pup = addCreatureReady(player1, new PacksongPup());
        harness.addToBattlefield(player1, new HowlpackWolf());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(pup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Packsong Pup does not count itself as another Wolf")
    void selfDoesNotSatisfyCondition() {
        Permanent pup = addCreatureReady(player1, new PacksongPup());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(pup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's Wolf does not satisfy the condition")
    void opponentsWolfDoesNotSatisfyCondition() {
        Permanent pup = addCreatureReady(player1, new PacksongPup());
        harness.addToBattlefield(player2, new HowlpackWolf());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(pup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("When Packsong Pup dies, its controller gains life equal to its power")
    void deathGainsEffectivePowerAsLife() {
        harness.setLife(player1, 10);
        Permanent pup = addCreatureReady(player1, new PacksongPup());
        harness.addToBattlefield(player1, new HowlpackWolf());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(pup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pup.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Another Werewolf satisfies the combat condition")
    void anotherWerewolfPutsCounterOnPup() {
        Permanent pup = addCreatureReady(player1, new PacksongPup());
        harness.addToBattlefield(player1, new ChildOfThePack());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(pup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple other Wolves still give only one counter")
    void multipleWolvesGiveOnlyOneCounter() {
        Permanent pup = addCreatureReady(player1, new PacksongPup());
        harness.addToBattlefield(player1, new PacksongPup());
        harness.addToBattlefield(player1, new PacksongPup());

        advanceToCombat(player1);
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }

        assertThat(pup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Packsong Pup does not trigger during an opponent's combat")
    void opponentsCombatDoesNotGiveCounter() {
        Permanent pup = addCreatureReady(player1, new PacksongPup());
        harness.addToBattlefield(player1, new PacksongPup());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(pup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing the only other Wolf before resolution prevents the counter")
    void conditionIsCheckedAgainOnResolution() {
        Permanent pup = addCreatureReady(player1, new PacksongPup());
        Permanent other = addCreatureReady(player1, new PacksongPup());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        advanceToCombat(player1);
        harness.castAndResolveInstant(player1, 0, other.getId());
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }

        assertThat(pup.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 21);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
