package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodexShredderTest extends BaseCardTest {

    @Test
    void millsOneCardFromTargetPlayersLibrary() {
        harness.addToBattlefield(player1, new CodexShredder());
        List<Card> library = gd.playerDecks.get(player2.getId());
        while (library.size() > 1) {
            library.removeFirst();
        }
        Card topCard = library.getFirst();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
    }

    @Test
    void sacrificesItselfAndReturnsTargetCardFromOwnGraveyard() {
        Card returnedCard = new CodexShredder();
        Card remainingCard = new CodexShredder();
        harness.addToBattlefield(player1, new CodexShredder());
        harness.setGraveyard(player1, List.of(returnedCard, remainingCard));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(returnedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(returnedCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(remainingCard)
                .doesNotContain(returnedCard);
        harness.assertInGraveyard(player1, "Codex Shredder");
        harness.assertNotOnBattlefield(player1, "Codex Shredder");
    }

    @Test
    void cannotTargetCardInOpponentsGraveyard() {
        Card opponentCard = new CodexShredder();
        harness.addToBattlefield(player1, new CodexShredder());
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
