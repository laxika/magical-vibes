package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacklingObserver.class, Forest.class, GrizzlyBears.class, LightningBolt.class, SerraAngel.class})
class CacklingObserverTest extends BaseCardTest {

    @Test
    void revealsOnlyNonlandCardsAndTracksTheChosenExile() {
        Card land = new Forest();
        Card chosen = new SerraAngel();
        Card otherNonland = new GrizzlyBears();
        harness.setHand(player2, List.of(land, chosen, otherNonland));

        castObserver();

        PendingInteraction.RevealedMatchingHandCardChoice choice = harnessInteraction();
        assertThat(choice.cards()).containsExactly(chosen, otherNonland);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        UUID observerId = harness.getPermanentId(player1, "Cackling Observer");
        assertThat(gd.getCardsExiledByPermanent(observerId)).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, otherNonland);
    }

    @Test
    void leavesTriggerMakesTheExiledCardOwnerSeekALesserManaValueNonlandCard() {
        Card chosen = new SerraAngel();
        harness.setHand(player2, List.of(chosen));
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears(), new SerraAngel()));

        castObserver();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        UUID observerId = harness.getPermanentId(player1, "Cackling Observer");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, observerId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
    }

    private PendingInteraction.RevealedMatchingHandCardChoice harnessInteraction() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealedMatchingHandCardChoice.class);
    }

    private void castObserver() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CacklingObserver()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
