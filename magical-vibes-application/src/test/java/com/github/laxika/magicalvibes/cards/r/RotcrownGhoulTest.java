package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RotcrownGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("When Rotcrown Ghoul dies, target player mills five cards")
    void deathMillsTargetPlayerFive() {
        harness.addToBattlefield(player2, new RotcrownGhoul());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        UUID ghoulId = harness.getPermanentId(player2, "Rotcrown Ghoul");
        harness.castInstant(player1, 0, ghoulId);
        harness.passBothPriorities(); // Doom Blade resolves → Ghoul dies → death trigger

        // Ghoul's controller (player2) chooses the target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 5);
    }

    @Test
    @DisplayName("Death trigger can target the Ghoul's own controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player2, new RotcrownGhoul());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        UUID ghoulId = harness.getPermanentId(player2, "Rotcrown Ghoul");
        harness.castInstant(player1, 0, ghoulId);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 5);
    }
}
