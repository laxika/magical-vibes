package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinWarStrikeTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new GoblinWarStrike()));
        harness.addMana(player1, ManaColor.RED, 1); // {R}
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals damage equal to the number of Goblins you control")
    void dealsDamageEqualToGoblinCount() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new RagingGoblin());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 2);
    }

    @Test
    @DisplayName("Deals no damage when you control no Goblins")
    void dealsNoDamageWithoutGoblins() {
        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before);
    }

    @Test
    @DisplayName("Counts only Goblins you control, not the opponent's")
    void countsOnlyControllersGoblins() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player2, new RagingGoblin());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }
}
