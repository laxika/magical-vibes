package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinWarStrike.class, GoblinPiker.class, RagingGoblin.class, BearCub.class,
        GarrukWildspeaker.class})
class GoblinWarStrikeTest extends BaseCardTest {

    private void prepareCast() {
        harness.setHand(player1, List.of(new GoblinWarStrike()));
        harness.addMana(player1, ManaColor.RED, 1); // {R}
    }

    private void cast(UUID targetId) {
        prepareCast();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals damage equal to the number of Goblins you control")
    void dealsDamageEqualToGoblinCount() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new RagingGoblin());

        int before = gd.getLife(player2.getId());
        cast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 2);
    }

    @Test
    @DisplayName("Deals no damage when you control no Goblins")
    void dealsNoDamageWithoutGoblins() {
        int before = gd.getLife(player2.getId());
        cast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(before);
    }

    @Test
    @DisplayName("Counts only Goblins you control, not the opponent's")
    void countsOnlyControllersGoblins() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player2, new RagingGoblin());

        int before = gd.getLife(player2.getId());
        cast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("Does not count non-Goblin creatures")
    void doesNotCountNonGoblins() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new BearCub());

        int before = gd.getLife(player2.getId());
        cast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetItsController() {
        harness.addToBattlefield(player1, new RagingGoblin());

        int before = gd.getLife(player1.getId());
        cast(player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(before - 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals damage to a target planeswalker")
    void dealsDamageToTargetPlaneswalker() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        cast(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts Goblins when the spell resolves")
    void countsGoblinsAtResolution() {
        harness.addToBattlefield(player1, new GoblinPiker());
        prepareCast();
        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new RagingGoblin());

        int before = gd.getLife(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 2);
    }
}
