package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshenSkinZubera.class, IsamaruHoundOfKonda.class, RendFlesh.class, RendSpirit.class})
class AshenSkinZuberaTest extends BaseCardTest {

    // "When this creature dies, target opponent discards a card for each Zubera that died this turn."

    private void startMainPhase(Card... spells) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(spells));
        harness.addMana(player1, ManaColor.BLACK, 6);
    }

    @Test
    @DisplayName("Dies alone: target opponent discards one card")
    void diesAloneDiscardsOne() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new AshenSkinZubera());
        harness.setHand(player2, List.of(new IsamaruHoundOfKonda(), new IsamaruHoundOfKonda()));
        startMainPhase(new RendSpirit());

        harness.castAndResolveInstant(player1, 0, zubera.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts every Zubera that died this turn, including itself")
    void countsAllZuberaDeathsThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AshenSkinZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AshenSkinZubera());
        harness.setHand(player2, List.of(
                new IsamaruHoundOfKonda(), new IsamaruHoundOfKonda(), new IsamaruHoundOfKonda()));
        startMainPhase(new RendSpirit(), new RendSpirit());

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Non-Zubera deaths do not increase the discard count")
    void nonZuberaDeathsDoNotCount() {
        Permanent nonZubera = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new AshenSkinZubera());
        harness.setHand(player2, List.of(new IsamaruHoundOfKonda(), new IsamaruHoundOfKonda()));
        startMainPhase(new RendFlesh(), new RendSpirit());

        harness.castAndResolveInstant(player1, 0, nonZubera.getId());

        harness.castAndResolveInstant(player1, 0, zubera.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts Zuberas that died under either player's control")
    void countsZuberaDeathsAcrossPlayers() {
        Permanent ownZubera = harness.addToBattlefieldAndReturn(player1, new AshenSkinZubera());
        Permanent opposingZubera = harness.addToBattlefieldAndReturn(player2, new AshenSkinZubera());
        harness.setHand(player2, List.of(new IsamaruHoundOfKonda(), new IsamaruHoundOfKonda()));
        startMainPhase(new RendSpirit(), new RendSpirit(), new RendSpirit());

        harness.castAndResolveInstant(player1, 0, opposingZubera.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.castAndResolveInstant(player1, 0, ownZubera.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
