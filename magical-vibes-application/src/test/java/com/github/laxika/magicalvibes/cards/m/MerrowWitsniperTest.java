package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MerrowWitsniperTest extends BaseCardTest {

    private void castWitsniper(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new MerrowWitsniper()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }

    @Test
    @DisplayName("Resolving puts Merrow Witsniper on battlefield with ETB trigger targeting the chosen player")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castWitsniper(player2.getId());
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Merrow Witsniper"));

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
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

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
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(9);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Mills nothing when target player's library is empty")
    void millsNothingWhenLibraryEmpty() {
        gd.playerDecks.get(player2.getId()).clear();

        castWitsniper(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
