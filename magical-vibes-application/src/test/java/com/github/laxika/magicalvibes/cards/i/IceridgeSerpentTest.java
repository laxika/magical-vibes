package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IceridgeSerpent.class, GrizzlyBears.class})
class IceridgeSerpentTest extends BaseCardTest {

    @Test
    void etbReturnsTargetedOpponentCreatureToItsOwnersHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSerpent(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Iceridge Serpent");
    }

    @Test
    void cannotTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new IceridgeSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearsId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSerpent(UUID targetId) {
        harness.setHand(player1, List.of(new IceridgeSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
