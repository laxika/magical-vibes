package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeSpy.class, BearCub.class, Plains.class})
class EyeSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Controller may put target player's top card into their graveyard")
    void putsTopCardIntoTargetGraveyardWhenAccepted() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card topCard = new BearCub();
        gd.playerDecks.get(player2.getId()).add(0, topCard);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Leaves the top card on the library when declined")
    void leavesTopCardWhenDeclined() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card topCard = new BearCub();
        gd.playerDecks.get(player2.getId()).add(0, topCard);
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Can target yourself and mill your own top card")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card topCard = new BearCub();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("May put a land card into target player's graveyard")
    void putsLandCardIntoTargetGraveyardWhenAccepted() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card topCard = new Plains();
        harness.setLibrary(player2, List.of(topCard));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Resolves with no effect when target library is empty")
    void emptyLibraryResolvesCleanly() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setLibrary(player2, List.of());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the top card moves and the remaining library keeps its order")
    void movesOnlyTopCardAndPreservesRemainingOrder() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card topCard = new BearCub();
        Card secondCard = new Plains();
        Card thirdCard = new BearCub();
        harness.setLibrary(player2, List.of(topCard, secondCard, thirdCard));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard, thirdCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card == topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the caster sees the top card before deciding whether to leave it")
    void privatelyShowsTopCardBeforeChoice() {
        harness.setHand(player1, List.of(new EyeSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card topCard = new BearCub();
        Card secondCard = new Plains();
        harness.setLibrary(player2, List.of(topCard, secondCard));
        harness.clearMessages();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("Bear Cub")).isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining("Bear Cub")).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, secondCard);
        assertThat(harness.getConn2().getMessagesContaining("Bear Cub")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
