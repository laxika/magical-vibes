package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FerraforYoungYew.class, GrizzlyBears.class})
class FerraforYoungYewTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates Saprolings equal to all counters on creatures controlled by the target player")
    void etbCountsAllCountersOnTargetPlayersCreatures() {
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        targetCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        targetCreature.setCounterCount(CounterType.CHARGE, 3);

        Permanent untargetedCreature = addCreatureReady(player1, new GrizzlyBears());
        untargetedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);

        harness.setHand(player1, List.of(new FerraforYoungYew()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(5);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Tapped ability doubles every kind of counter on a target creature")
    void activatedAbilityDoublesEveryKindOfCounter() {
        Permanent ferrafor = addCreatureReady(player1, new FerraforYoungYew());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        targetCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        targetCreature.setCounterCount(CounterType.CHARGE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(ferrafor.isTapped()).isTrue();
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(targetCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }
}
