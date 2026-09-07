package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesperateBloodseeker.class})
class DesperateBloodseekerTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, target opponent mills two cards")
    void etbMillsTargetOpponent() {
        harness.setLibrary(player2, List.of(new DesperateBloodseeker(), new DesperateBloodseeker(),
                new DesperateBloodseeker()));
        castDesperateBloodseeker();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The controller may target themselves")
    void etbMillsController() {
        harness.setLibrary(player1, List.of(new DesperateBloodseeker(), new DesperateBloodseeker()));
        castDesperateBloodseeker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private void castDesperateBloodseeker() {
        harness.setHand(player1, List.of(new DesperateBloodseeker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
    }
}
