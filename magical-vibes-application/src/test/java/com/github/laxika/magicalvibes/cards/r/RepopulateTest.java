package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BloatedToad;
import com.github.laxika.magicalvibes.cards.i.IronWill;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Repopulate.class, BloatedToad.class, IronWill.class})
class RepopulateTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles all creature cards from the target player's graveyard into their library")
    void shufflesCreatureCardsFromTargetGraveyard() {
        BloatedToad creature1 = new BloatedToad();
        BloatedToad creature2 = new BloatedToad();
        IronWill noncreature = new IronWill();
        harness.setGraveyard(player2, List.of(creature1, noncreature, creature2));
        harness.setHand(player1, List.of(new Repopulate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 2)
                .contains(creature1, creature2);
    }

    @Test
    @DisplayName("Leaves a noncreature-only graveyard unchanged")
    void leavesGraveyardUnchangedWhenNoCreatureCardsMatch() {
        IronWill noncreature = new IronWill();
        IronWill libraryCard = new IronWill();
        harness.setGraveyard(player2, List.of(noncreature));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new Repopulate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Only a player can be targeted")
    void rejectsPermanentTarget() {
        UUID permanentId = harness.addToBattlefieldAndReturn(player2, new BloatedToad()).getId();
        harness.setHand(player1, List.of(new Repopulate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Repopulate and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Repopulate()));
        harness.setLibrary(player1, List.of(new BloatedToad()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Repopulate");
        harness.assertInHand(player1, "Bloated Toad");
    }
}
