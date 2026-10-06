package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamingPhantom.class, Forest.class})
class ScreamingPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills the top card of its controller's library")
    void attackingMillsTopCard() {
        Card milled = new Forest();
        harness.setLibrary(player1, List.of(milled));
        addCreatureReady(player1, new ScreamingPhantom());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
    }

    @Test
    @DisplayName("Attacking mills exactly the top card and leaves the opponent's library alone")
    void attackingMillsOnlyOneCard() {
        Card top = new Forest();
        Card remaining = new Forest();
        Card opponentCard = new Forest();
        harness.setLibrary(player1, List.of(top, remaining));
        harness.setLibrary(player2, List.of(opponentCard));
        addCreatureReady(player1, new ScreamingPhantom());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Phantom controlled by the other player mills that player's library")
    void otherControllerMillsTheirOwnLibrary() {
        Card milled = new Forest();
        Card untouched = new Forest();
        harness.setLibrary(player2, List.of(milled));
        harness.setLibrary(player1, List.of(untouched));
        addCreatureReady(player2, new ScreamingPhantom());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(milled);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking with an empty library resolves without losing the game")
    void attackingWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new ScreamingPhantom());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    @DisplayName("A Phantom that stays out of combat does not trigger when another Phantom attacks")
    void onlyAttackingPhantomTriggers() {
        Card top = new Forest();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(top, remaining));
        addCreatureReady(player1, new ScreamingPhantom());
        addCreatureReady(player1, new ScreamingPhantom());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
    }
}
