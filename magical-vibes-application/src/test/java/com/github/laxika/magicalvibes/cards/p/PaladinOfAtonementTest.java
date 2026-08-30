package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaladinOfAtonementTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself during each upkeep after its controller lost life")
    void growsDuringUpkeepAfterControllerLostLife() {
        harness.addToBattlefield(player1, new PaladinOfAtonement());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Paladin of Atonement");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself during upkeep without life loss last turn")
    void doesNotGrowWithoutControllerLifeLoss() {
        harness.addToBattlefield(player1, new PaladinOfAtonement());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Paladin of Atonement");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gains life equal to its toughness when it dies")
    void gainsLifeEqualToToughnessOnDeath() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PaladinOfAtonement());
        paladin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID paladinId = paladin.getId();
        harness.castInstant(player2, 0, paladinId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }
}
