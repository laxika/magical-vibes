package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VanguardSeraph.class, AngelOfMercy.class, GrizzlyBears.class, HillGiant.class})
class VanguardSeraphTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 on the first life gain each turn")
    void surveilsOnFirstLifeGainEachTurn() {
        Card topCard = new GrizzlyBears();
        prepareLibrary(topCard);
        harness.addToBattlefield(player1, new VanguardSeraph());
        castAngelAndResolveLifeGain();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Does not surveil again after another life gain in the same turn")
    void doesNotSurveilAgainInSameTurn() {
        Card topCard = new GrizzlyBears();
        prepareLibrary(topCard);
        harness.addToBattlefield(player1, new VanguardSeraph());
        harness.setHand(player1, List.of(new AngelOfMercy(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveils again on the first life gain of a new turn")
    void surveilsAgainOnNewTurn() {
        Card firstTopCard = new GrizzlyBears();
        Card drawCard = new HillGiant();
        Card secondTopCard = new GrizzlyBears();
        prepareLibrary(firstTopCard, drawCard, secondTopCard);
        harness.addToBattlefield(player1, new VanguardSeraph());

        castAngelAndResolveLifeGain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castAngelAndResolveLifeGain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondTopCard);
    }

    @Test
    @DisplayName("Does not trigger if life was gained before Seraph entered this turn")
    void doesNotTriggerAfterEarlierLifeGainBeforeEntry() {
        Card topCard = new GrizzlyBears();
        prepareLibrary(topCard);
        castAngelAndResolveLifeGain();
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player1, new VanguardSeraph());
        castAngelAndResolveLifeGain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Opponent life gain does not trigger Seraph")
    void opponentLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new VanguardSeraph());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Surveilling an empty library completes without a choice")
    void surveilEmptyLibrary() {
        prepareLibrary();
        harness.addToBattlefield(player1, new VanguardSeraph());
        castAngelAndResolveLifeGain();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 23);
    }

    private void castAngelAndResolveLifeGain() {
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
