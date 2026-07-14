package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrazingKelpieTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData g = harness.getGameData();
            if (g.interaction.isAwaitingInput() || g.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private int kelpieIndex(Permanent kelpie) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(kelpie);
    }

    @Test
    @DisplayName("Tucks a card from its owner's graveyard onto the bottom of that owner's library")
    void tucksCardToBottomOfOwnersLibrary() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new GrazingKelpie());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Card tucked = new HillGiant();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant(), new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, kelpieIndex(kelpie), 0, List.of(tucked.getId()));
        resolveUntilInputOrEmpty();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(library.size() - 1).getId()).isEqualTo(tucked.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(tucked.getId()));
    }

    @Test
    @DisplayName("A targeted opponent's card goes to the bottom of the opponent's library")
    void tucksOpponentCardToOpponentLibrary() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new GrazingKelpie());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card tucked = new HillGiant();
        harness.setGraveyard(player2, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, kelpieIndex(kelpie), 0, List.of(tucked.getId()));
        resolveUntilInputOrEmpty();

        List<Card> opponentLibrary = gd.playerDecks.get(player2.getId());
        assertThat(opponentLibrary).hasSize(2);
        assertThat(opponentLibrary.get(opponentLibrary.size() - 1).getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Persist returns the sacrificed Kelpie with a -1/-1 counter")
    void persistReturnsSacrificedKelpie() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new GrazingKelpie());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Card tucked = new HillGiant();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, kelpieIndex(kelpie), 0, List.of(tucked.getId()));
        resolveUntilInputOrEmpty();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grazing Kelpie"))
                .findFirst().orElse(null);
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
