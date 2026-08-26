package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DeathlessKnight.class)
class DeathlessKnightTest extends BaseCardTest {

    @Test
    void returnsFromGraveyardWhenControllerGainsLife() {
        DeathlessKnight knight = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(knight));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        assertThat(gd.stack).singleElement()
                .extracting(entry -> entry.getEntryType())
                .isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(knight);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(knight);
    }

    @Test
    void triggersOnlyForFirstLifeGainEachTurn() {
        DeathlessKnight knight = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(knight));

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        });

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(knight);
    }

    @Test
    void opponentLifeGainDoesNotTriggerIt() {
        DeathlessKnight knight = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(knight));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(knight);
    }
}
