package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MerrowWitsniper.class)
class MerrowWitsniperTest extends BaseCardTest {

    private void castWitsniper(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new MerrowWitsniper()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Resolving puts Merrow Witsniper on battlefield with ETB trigger targeting the chosen player")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castWitsniper(player2.getId());
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Merrow Witsniper");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger mills one card from target player's library")
    void etbMillsOneCard() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        castWitsniper(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(9);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        castWitsniper(player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(9);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Mills nothing when target player's library is empty")
    void millsNothingWhenLibraryEmpty() {
        gd.playerDecks.get(player2.getId()).clear();

        castWitsniper(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the top card is milled, leaving the other player's library untouched")
    void millsOnlyTopCardOfTargetLibrary() {
        Card top = new MerrowWitsniper();
        Card next = new MerrowWitsniper();
        Card ownTop = new MerrowWitsniper();
        harness.setLibrary(player2, List.of(top, next));
        harness.setLibrary(player1, List.of(ownTop));

        castWitsniper(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling the last card does not cause a player to lose")
    void millingLastCardDoesNotCauseLoss() {
        Card last = new MerrowWitsniper();
        harness.setLibrary(player2, List.of(last));

        castWitsniper(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(last);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The mill trigger resolves even after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Card top = new MerrowWitsniper();
        harness.setLibrary(player2, List.of(top));

        castWitsniper(player2.getId());
        harness.passBothPriorities();

        var source = findPermanent(player1, "Merrow Witsniper");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.setHand(player1, List.of(source.getCard()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
