package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.cards.r.RushOfAdrenaline;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HermitOfTheNatterknolls.class, QuilledWolf.class, RushOfAdrenaline.class})
class HermitOfTheNatterknollsTest extends BaseCardTest {

    @Test
    @DisplayName("Front face draws one when an opponent casts a spell during your turn")
    void frontFaceDrawsOneOnOpponentSpellDuringYourTurn() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        harness.setLibrary(player1, List.of(new QuilledWolf(), new QuilledWolf(), new QuilledWolf()));
        forceMainPhase(player1);
        harness.setHand(player2, List.of(new RushOfAdrenaline()));
        harness.addMana(player2, ManaColor.RED, 1);

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castInstant(player2, 0, hermit.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
    }

    @Test
    @DisplayName("Front face does not draw when an opponent casts a spell during their turn")
    void frontFaceDoesNotDrawOnOpponentTurn() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        harness.setLibrary(player1, List.of(new QuilledWolf(), new QuilledWolf(), new QuilledWolf()));
        forceMainPhase(player2);
        harness.setHand(player2, List.of(new RushOfAdrenaline()));
        harness.addMana(player2, ManaColor.RED, 1);

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Hermit of the Natterknolls"));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
    }

    @Test
    @DisplayName("Back face draws two when an opponent casts a spell during your turn")
    void backFaceDrawsTwoOnOpponentSpellDuringYourTurn() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        harness.setLibrary(player1, List.of(
                new QuilledWolf(), new QuilledWolf(), new QuilledWolf(), new QuilledWolf()));
        transformToBackFace(player1, hermit);
        forceMainPhase(player1);
        harness.setHand(player2, List.of(new RushOfAdrenaline()));
        harness.addMana(player2, ManaColor.RED, 1);

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castInstant(player2, 0, hermit.getId());
        resolveAllTriggers();

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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Neither face draws for its controller's own spell")
    void doesNotDrawForControllerSpell(boolean backFace) {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        if (backFace) {
            transformToBackFace(player1, hermit);
        }
        forceMainPhase(player1);
        harness.setLibrary(player1, List.of(new QuilledWolf(), new QuilledWolf(), new QuilledWolf()));
        harness.setHand(player1, List.of(new RushOfAdrenaline()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hermit.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Back face does not draw for an opponent's spell on that opponent's turn")
    void backFaceDoesNotDrawOnOpponentTurn() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        transformToBackFace(player1, hermit);
        forceMainPhase(player2);
        harness.setLibrary(player1, List.of(new QuilledWolf(), new QuilledWolf(), new QuilledWolf()));
        harness.setHand(player2, List.of(new RushOfAdrenaline()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hermit.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @ParameterizedTest
    @CsvSource({"1, 0", "0, 1"})
    @DisplayName("Front face does not trigger when either player cast a spell last turn")
    void frontFaceDoesNotTransformAfterASpell(int controllerSpells, int opponentSpells) {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), controllerSpells);
        gd.spellsCastLastTurn.put(player2.getId(), opponentSpells);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(hermit.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Front face transforms at the opponent's upkeep after a spell-free turn")
    void frontFaceTransformsAtOpponentUpkeep() {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolveTransform(player2);

        assertThat(hermit.isTransformed()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0, 0, false", "1, 0, false", "0, 1, false", "1, 1, false", "2, 0, true", "0, 3, true"})
    @DisplayName("Back face requires two spells from a single player, not two spells total")
    void backFaceChecksSpellsPerPlayer(int controllerSpells, int opponentSpells, boolean transforms) {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        transformToBackFace(player1, hermit);
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), controllerSpells);
        gd.spellsCastLastTurn.put(player2.getId(), opponentSpells);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(transforms ? 1 : 0);
        resolveAllTriggers();
        assertThat(hermit.isTransformed()).isEqualTo(!transforms);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("A draw trigger still resolves after its source leaves the battlefield")
    void drawTriggerSurvivesSourceLeaving(boolean backFace) {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        if (backFace) {
            transformToBackFace(player1, hermit);
        }
        forceMainPhase(player1);
        harness.setLibrary(player1, List.of(new QuilledWolf(), new QuilledWolf(), new QuilledWolf()));
        harness.setHand(player2, List.of(new RushOfAdrenaline()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hermit.getId());
        gd.playerBattlefields.get(player1.getId()).remove(hermit);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(backFace ? 1 : 2);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("An opponent's spell in response to an upkeep transform draws for the current face")
    void opponentSpellInResponseToTransform(boolean backFace) {
        harness.addToBattlefield(player1, new HermitOfTheNatterknolls());
        Permanent hermit = findPermanent(player1, "Hermit of the Natterknolls");
        if (backFace) {
            transformToBackFace(player1, hermit);
        }
        gd.spellsCastLastTurn.clear();
        if (backFace) {
            gd.spellsCastLastTurn.put(player2.getId(), 2);
        }
        harness.setLibrary(player1, List.of(new QuilledWolf(), new QuilledWolf(), new QuilledWolf()));
        harness.setHand(player2, List.of(new RushOfAdrenaline()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, hermit.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(backFace ? 1 : 2);
        assertThat(hermit.isTransformed()).isEqualTo(!backFace);
    }

    private void transformToBackFace(Player activePlayer, Permanent hermit) {
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolveTransform(activePlayer);
        assertThat(hermit.isTransformed()).isTrue();
    }

    private void advanceToUpkeepAndResolveTransform(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        resolveAllTriggers();
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
