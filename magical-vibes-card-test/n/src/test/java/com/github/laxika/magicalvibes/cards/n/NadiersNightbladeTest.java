package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NadiersNightblade.class, SolRing.class})
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

        assertThat(gd.stack).isEmpty();

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

        assertThat(gd.stack).isEmpty();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Exiling a token triggers the drain")
    void exilingTokenDrains() {
        harness.addToBattlefield(player1, new NadiersNightblade());
        Permanent token = harness.addToBattlefieldAndReturn(player1, solRingToken());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, token));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Returning a token to hand triggers the drain")
    void returningTokenToHandDrains() {
        harness.addToBattlefield(player1, new NadiersNightblade());
        Permanent token = harness.addToBattlefieldAndReturn(player1, solRingToken());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, token));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Each token leaving simultaneously triggers separately, even when Nightblade also leaves")
    void simultaneousRemovalDrainsForEachToken() {
        Permanent nightblade = harness.addToBattlefieldAndReturn(player1, new NadiersNightblade());
        Permanent first = harness.addToBattlefieldAndReturn(player1, solRingToken());
        Permanent second = harness.addToBattlefieldAndReturn(player1, solRingToken());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, List.of(nightblade, first, second), () -> {
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, nightblade);
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
                }));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A token copy of Nadier's Nightblade triggers when it itself leaves")
    void tokenNightbladeTriggersForItsOwnDeparture() {
        NadiersNightblade copy = new NadiersNightblade();
        copy.setToken(true);
        Permanent nightblade = harness.addToBattlefieldAndReturn(player1, copy);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        removeFromBattlefield(nightblade);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private Card solRingToken() {
        Card card = new SolRing();
        card.setToken(true);
        return card;
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
