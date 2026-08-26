package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VulturousZombie.class, Shock.class, TomeScour.class})
class VulturousZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's spell is put into their graveyard")
    void getsCounterWhenOpponentSpellIsPutIntoGraveyard() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets one counter for each opponent card milled")
    void getsCounterForEachOpponentCardMilled() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        for (int i = 0; i < 5; i++) {
            harness.passBothPriorities();
        }

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger for a card put into the controller's graveyard")
    void doesNotTriggerForOwnCard() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new VulturousZombie());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
