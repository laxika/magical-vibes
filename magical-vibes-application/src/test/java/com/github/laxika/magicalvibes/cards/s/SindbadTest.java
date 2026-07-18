package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SindbadTest extends BaseCardTest {

    @Test
    @DisplayName("Drawn land card is kept in hand")
    void drawnLandIsKept() {
        addReadySindbad(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("Drawn nonland card is revealed and discarded")
    void drawnNonlandIsDiscarded() {
        addReadySindbad(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private void addReadySindbad(Player player) {
        // Added at battlefield index 0 so activateAbility(player, 0, ...) targets it; not
        // summoning sick so its {T} ability can be activated immediately.
        Permanent perm = new Permanent(new Sindbad());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
