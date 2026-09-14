package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MoggJailer;
import com.github.laxika.magicalvibes.cards.q.QuirionExplorer;
import com.github.laxika.magicalvibes.cards.s.StormscapeFamiliar;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntingDrake.class, MoggJailer.class, QuirionExplorer.class, StormscapeFamiliar.class,
        TerminalMoraine.class})
class HuntingDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a target red creature on top of its owner's library")
    void etbPutsTargetRedCreatureOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new MoggJailer());
        UUID targetId = harness.getPermanentId(player2, "Mogg Jailer");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castHuntingDrake(targetId);

        harness.assertNotOnBattlefield(player2, "Mogg Jailer");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Mogg Jailer");
    }

    @Test
    @DisplayName("ETB puts a target green creature on top of its owner's library")
    void etbPutsTargetGreenCreatureOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new QuirionExplorer());
        UUID targetId = harness.getPermanentId(player2, "Quirion Explorer");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castHuntingDrake(targetId);

        harness.assertNotOnBattlefield(player2, "Quirion Explorer");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Quirion Explorer");
    }

    @Test
    @DisplayName("ETB can target a red creature its controller controls")
    void etbCanTargetRedCreatureItsControllerControls() {
        harness.addToBattlefield(player1, new MoggJailer());
        UUID targetId = harness.getPermanentId(player1, "Mogg Jailer");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castHuntingDrake(targetId);

        harness.assertNotOnBattlefield(player1, "Mogg Jailer");
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Mogg Jailer");
    }

    @Test
    @DisplayName("Cannot target a nonred nongreen creature")
    void cannotTargetNonRedNonGreenCreature() {
        harness.addToBattlefield(player2, new StormscapeFamiliar());
        UUID targetId = harness.getPermanentId(player2, "Stormscape Familiar");

        harness.setHand(player1, List.of(new HuntingDrake()));
        addHuntingDrakeMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new TerminalMoraine());
        UUID targetId = harness.getPermanentId(player2, "Terminal Moraine");

        harness.setHand(player1, List.of(new HuntingDrake()));
        addHuntingDrakeMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target is removed before resolution")
    void fizzlesIfTargetIsRemoved() {
        harness.addToBattlefield(player2, new QuirionExplorer());
        UUID targetId = harness.getPermanentId(player2, "Quirion Explorer");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new HuntingDrake()));
        addHuntingDrakeMana();
        harness.castCreature(player1, 0, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gameData.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("fizzles"));
    }

    private void castHuntingDrake(UUID targetId) {
        harness.setHand(player1, List.of(new HuntingDrake()));
        addHuntingDrakeMana();

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addHuntingDrakeMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
