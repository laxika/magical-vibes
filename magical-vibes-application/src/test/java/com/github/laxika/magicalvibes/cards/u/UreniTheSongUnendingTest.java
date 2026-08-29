package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UreniTheSongUnending.class, Forest.class, GarrukWildspeaker.class, GrizzlyBears.class})
class UreniTheSongUnendingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals the land-count damage among opponent creatures and planeswalkers")
    void etbDealsLandCountDamageAmongTargets() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 1, planeswalker.getId(), 2);

        castUreni(List.of(creature.getId(), planeswalker.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB cannot target a creature its controller controls")
    void etbRejectsOwnCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker an opponent controls");
    }

    private void castUreni(List<java.util.UUID> targetIds) {
        prepareToCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareToCast() {
        harness.setHand(player1, List.of(new UreniTheSongUnending()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
