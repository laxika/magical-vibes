package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuseDrake.class, Island.class})
class MuseDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new MuseDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLibrary(player1, List.of(new Island()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Muse Drake");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("ETB draws exactly one card for its controller only when the trigger resolves")
    void drawWaitsForTriggerResolutionAndOnlyAffectsController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new MuseDrake()));
        MuseDrake topCard = new MuseDrake();
        MuseDrake remainingCard = new MuseDrake();
        harness.setLibrary(player2, List.of(topCard, remainingCard));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Muse Drake");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, remainingCard);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
