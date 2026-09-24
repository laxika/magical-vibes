package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LupineHarbingers.class)
class LupineHarbingersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter after one of its controller's turns begins")
    void entersWithCountersForTurnsBegunSinceForetell() {
        LupineHarbingers lupine = new LupineHarbingers();
        harness.setHand(player1, List.of(lupine));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        gd.turnsTakenByPlayer.merge(player1.getId(), 1, Integer::sum);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, lupine.getId());
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Lupine Harbingers");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters without counters when it was not foretold")
    void hardCastEntersWithoutCounters() {
        harness.setHand(player1, List.of(new LupineHarbingers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Lupine Harbingers");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
