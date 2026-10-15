package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KiorasFollower;
import com.github.laxika.magicalvibes.cards.n.NyxbornTriton;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SirenOfTheSilentSong.class, NyxbornTriton.class, KiorasFollower.class})
class SirenOfTheSilentSongTest extends BaseCardTest {

    @Test
    void untappingMakesEachOpponentDiscardThenMill() {
        addTappedSiren();
        harness.setHand(player2, List.of(new NyxbornTriton()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        runUntapStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Nyxborn Triton");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    void emptyOpponentHandStillMills() {
        addTappedSiren();
        harness.setHand(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        runUntapStep();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    void opponentChoosesDiscardBeforeTopCardIsMilledAndControllerIsUnaffected() {
        addTappedSiren();
        NyxbornTriton discarded = new NyxbornTriton();
        NyxbornTriton kept = new NyxbornTriton();
        NyxbornTriton milled = new NyxbornTriton();
        NyxbornTriton controllerHand = new NyxbornTriton();
        NyxbornTriton controllerLibrary = new NyxbornTriton();
        harness.setHand(player2, List.of(kept, discarded));
        harness.setLibrary(player2, List.of(milled));
        harness.setHand(player1, List.of(controllerHand));
        harness.setLibrary(player1, List.of(controllerLibrary));

        runUntapStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(milled);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(discarded, milled);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded, milled);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibrary);
    }

    @Test
    void emptyOpponentLibraryDoesNotPreventDiscard() {
        addTappedSiren();
        NyxbornTriton discarded = new NyxbornTriton();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of());

        runUntapStep();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void alreadyUntappedSirenDoesNotTrigger() {
        harness.addToBattlefield(player1, new SirenOfTheSilentSong());
        NyxbornTriton opponentHand = new NyxbornTriton();
        NyxbornTriton opponentLibrary = new NyxbornTriton();
        harness.setHand(player2, List.of(opponentHand));
        harness.setLibrary(player2, List.of(opponentLibrary));

        runUntapStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibrary);
    }

    @Test
    void untappingOutsideUntapStepAlsoTriggers() {
        Permanent siren = addTappedSiren();
        addCreatureReady(player1, new KiorasFollower());
        NyxbornTriton discarded = new NyxbornTriton();
        NyxbornTriton milled = new NyxbornTriton();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(milled));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, siren.getId());
        harness.passBothPriorities();

        assertThat(siren.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(milled);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded, milled);
    }

    @Test
    void opponentsAreDeterminedBySirensController() {
        Permanent siren = harness.addToBattlefieldAndReturn(player2, new SirenOfTheSilentSong());
        siren.tap();
        NyxbornTriton discarded = new NyxbornTriton();
        NyxbornTriton milled = new NyxbornTriton();
        NyxbornTriton controllerHand = new NyxbornTriton();
        NyxbornTriton controllerLibrary = new NyxbornTriton();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(milled));
        harness.setHand(player2, List.of(controllerHand));
        harness.setLibrary(player2, List.of(controllerLibrary));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded, milled);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(controllerHand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(controllerLibrary);
    }

    private Permanent addTappedSiren() {
        Permanent siren = harness.addToBattlefieldAndReturn(player1, new SirenOfTheSilentSong());
        siren.setSummoningSick(false);
        siren.tap();
        return siren;
    }

    private void runUntapStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
