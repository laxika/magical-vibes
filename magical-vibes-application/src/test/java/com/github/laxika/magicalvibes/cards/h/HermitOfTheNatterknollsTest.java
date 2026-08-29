package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HermitOfTheNatterknollsTest extends BaseCardTest {

    @Test
    @DisplayName("Front face draws one when an opponent casts a spell during your turn")
    void frontFaceDrawsOneOnOpponentSpellDuringYourTurn() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        forceMainPhase(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
    }

    @Test
    @DisplayName("Front face does not draw when an opponent casts a spell during their turn")
    void frontFaceDoesNotDrawOnOpponentTurn() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        forceMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
    }

    @Test
    @DisplayName("Back face draws two when an opponent casts a spell during your turn")
    void backFaceDrawsTwoOnOpponentSpellDuringYourTurn() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        transformToBackFace(player1, hermit);
        forceMainPhase(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 2);
    }

    @Test
    @DisplayName("Front face transforms when no spells were cast last turn")
    void transformsToBackFaceWhenNoSpellsWereCastLastTurn() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolveTransform(player1);

        assertThat(hermit.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Back face transforms when a player cast two or more spells last turn")
    void transformsToFrontFaceWhenTwoSpellsWereCastLastTurn() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        transformToBackFace(player1, hermit);
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeepAndResolveTransform(player2);

        assertThat(hermit.isTransformed()).isFalse();
    }

    private void transformToBackFace(Player activePlayer, Permanent hermit) {
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolveTransform(activePlayer);
        assertThat(hermit.isTransformed()).isTrue();
    }

    private void advanceToUpkeepAndResolveTransform(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
