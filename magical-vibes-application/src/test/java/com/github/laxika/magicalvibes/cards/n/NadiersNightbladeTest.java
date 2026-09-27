package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NadiersNightblade.class})
class NadiersNightbladeTest extends BaseCardTest {

    @Test
    @DisplayName("A token you control leaving the battlefield drains each opponent and gains you life")
    void ownTokenLeavingDrainsEachOpponent() {
        harness.addToBattlefield(player1, new NadiersNightblade());
        Permanent token = harness.addToBattlefieldAndReturn(player1, token("Treasure Token"));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        removeFromBattlefield(token);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A token an opponent controls does not trigger Nadier's Nightblade")
    void opponentTokenLeavingDoesNotTrigger() {
        harness.addToBattlefield(player1, new NadiersNightblade());
        Permanent token = harness.addToBattlefieldAndReturn(player2, token("Opponent Treasure Token"));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        removeFromBattlefield(token);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A nontoken permanent leaving the battlefield does not trigger Nadier's Nightblade")
    void nontokenPermanentLeavingDoesNotTrigger() {
        harness.addToBattlefield(player1, new NadiersNightblade());
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, nontokenArtifact("Clue"));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        removeFromBattlefield(permanent);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void removeFromBattlefield(Permanent permanent) {
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
    }

    private Card token(String name) {
        Card card = nontokenArtifact(name);
        card.setToken(true);
        return card;
    }

    private Card nontokenArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }
}
