package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RealityStrobe.class, GrizzlyBears.class})
class RealityStrobeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent and exiles Reality Strobe with three time counters")
    void returnsTargetPermanentAndExilesWithSuspendCounters() {
        RealityStrobe strobe = new RealityStrobe();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(strobe));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(strobe);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(strobe.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Suspend exiles Reality Strobe with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        RealityStrobe strobe = new RealityStrobe();
        harness.setHand(player1, List.of(strobe));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(strobe);
        assertThat(gd.exiledCardTimeCounters).containsEntry(strobe.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }
}
