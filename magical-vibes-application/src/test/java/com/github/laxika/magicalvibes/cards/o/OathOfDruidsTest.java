package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OathOfDruidsTest extends BaseCardTest {

    @Test
    @DisplayName("The active player reveals their library and puts the first creature onto the battlefield")
    void activePlayerRevealsTheirLibrary() {
        harness.addToBattlefield(player1, new OathOfDruids());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupLibrary(player1, new Forest(), new GrizzlyBears());

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Oath of Druids", "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The active player reveals their own library when another player controls the Oath")
    void activePlayerOwnsRevealWhenOpponentControlsOath() {
        harness.addToBattlefield(player1, new OathOfDruids());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setupLibrary(player1, new Forest());
        setupLibrary(player2, new Forest(), new GrizzlyBears());

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Oath of Druids", "Grizzly Bears");
    }

    @Test
    @DisplayName("The active player may decline the reveal")
    void mayDeclineReveal() {
        harness.addToBattlefield(player1, new OathOfDruids());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupLibrary(player1, new Forest(), new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Oath of Druids");
    }

    private void setupLibrary(Player player, Card... cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(Arrays.asList(cards));
    }
}
