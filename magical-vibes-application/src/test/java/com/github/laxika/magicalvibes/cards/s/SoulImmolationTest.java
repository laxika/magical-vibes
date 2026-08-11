package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulImmolationTest extends BaseCardTest {

    @Test
    void blightsChosenCreatureAndDamagesEachOpponentAndTheirCreatures() {
        Permanent blightCreature = addCreatureReady(player1, new ColossalDreadmaw());
        addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new SoulImmolation()));
        harness.addMana(player1, ManaColor.RED, 5);

        gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false, blightCreature.getId());

        assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void cannotAnnounceMoreThanGreatestControlledToughness() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulImmolation()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 4, null, null,
                List.of(), List.of(), false, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X can't be greater than 3");

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
