package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EyeSpyTest extends BaseCardTest {

    // ===== Accepted: top card goes to target player's graveyard =====

    @Test
    @DisplayName("Controller may put target player's top card into their graveyard")
    void putsTopCardIntoTargetGraveyardWhenAccepted() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).add(0, topCard);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
    }

    // ===== Declined: card stays on top of target player's library =====

    @Test
    @DisplayName("Leaves the top card on the library when declined")
    void leavesTopCardWhenDeclined() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).add(0, topCard);
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
    }

    // ===== Can target self =====

    @Test
    @DisplayName("Can target yourself and mill your own top card")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    // ===== Empty library: no prompt, spell resolves =====

    @Test
    @DisplayName("Resolves with no effect when target library is empty")
    void emptyLibraryResolvesCleanly() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gd.playerDecks.get(player2.getId()).clear();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
