package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Card secondTopCard = new GrizzlyBears();
        prepareLibrary(firstTopCard, secondTopCard);
        harness.addToBattlefield(player1, new VanguardSeraph());

        castAngelAndResolveLifeGain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        castAngelAndResolveLifeGain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondTopCard);
    }

    private void castAngelAndResolveLifeGain() {
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
