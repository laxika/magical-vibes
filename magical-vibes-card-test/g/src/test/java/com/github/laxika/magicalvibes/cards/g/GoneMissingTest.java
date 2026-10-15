package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DrownyardExplorers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoneMissing.class, Forest.class, DrownyardExplorers.class})
class GoneMissingTest extends BaseCardTest {

    @Test
    @DisplayName("Puts any target permanent on top of its owner's library and investigates")
    void putsTargetPermanentOnTopAndInvestigates() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new GoneMissing()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gameData.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 1)
                .first()
                .extracting(Card::getName)
                .isEqualTo("Forest");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing if the target permanent is removed before resolution")
    void fizzlesIfTargetIsRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DrownyardExplorers()).getId();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new GoneMissing()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can target your own permanent and the Clue can be sacrificed to draw it")
    void targetsOwnPermanentAndCreatesUsableClue() {
        Forest forest = new Forest();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, forest).getId();
        harness.setHand(player1, List.of(new GoneMissing()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).first().isSameAs(forest);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Clue");
        harness.assertNotInHand(player1, "Forest");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Clue");
    }

    @Test
    @DisplayName("Returns a stolen permanent to its owner's library rather than its controller's")
    void returnsStolenPermanentToOwnersLibrary() {
        Forest forest = new Forest();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, forest).getId();
        gd.stolenCreatures.put(targetId, player2.getId());
        int controllerLibrarySize = gd.playerDecks.get(player1.getId()).size();
        int ownerLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new GoneMissing()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(controllerLibrarySize);
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(ownerLibrarySize + 1).first().isSameAs(forest);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Can target a Clue token and still investigates after it leaves")
    void targetingTokenStillInvestigates() {
        harness.setHand(player1, List.of(new DrownyardExplorers(), new GoneMissing()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        UUID oldClueId = harness.getPermanentId(player1, "Clue");
        int librarySize = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, oldClueId);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(harness.getPermanentId(player1, "Clue")).isNotEqualTo(oldClueId);
    }
}
