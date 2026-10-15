package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.o.OrneryGoblin;
import com.github.laxika.magicalvibes.cards.r.RitualOfSoot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightReaper.class, DirectCurrent.class, OrneryGoblin.class, RitualOfSoot.class})
class MidnightReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature dying deals damage and draws a card")
    void anotherNontokenCreatureDies() {
        Card drawnCard = new DirectCurrent();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MidnightReaper());
        harness.addToBattlefield(player1, new OrneryGoblin());

        killCreatureWithDirectCurrent(player2, player1, "Ornery Goblin");

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Midnight Reaper dying triggers its ability")
    void midnightReaperDies() {
        Card drawnCard = new DirectCurrent();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MidnightReaper());

        killCreatureWithDirectCurrent(player2, player1, "Midnight Reaper");

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Midnight Reaper");
    }

    @Test
    @DisplayName("A token creature dying does not trigger Midnight Reaper")
    void tokenCreatureDies() {
        harness.setHand(player1, List.of());
        Card libraryCard = new DirectCurrent();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MidnightReaper());
        Card token = new OrneryGoblin();
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        killCreatureWithDirectCurrent(player2, player1, "Ornery Goblin");

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }


    @Test
    @DisplayName("A token Midnight Reaper does not trigger for its own death")
    void tokenMidnightReaperDies() {
        Card libraryCard = new DirectCurrent();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);
        Card tokenReaper = new MidnightReaper();
        tokenReaper.setToken(true);
        harness.addToBattlefield(player1, tokenReaper);

        killCreatureWithDirectCurrent(player2, player1, "Midnight Reaper");

        harness.assertNotOnBattlefield(player1, "Midnight Reaper");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Midnight Reaper")
    void opponentCreatureDies() {
        Card libraryCard = new DirectCurrent();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MidnightReaper());
        harness.addToBattlefield(player2, new OrneryGoblin());

        killCreatureWithDirectCurrent(player2, player2, "Ornery Goblin");

        harness.assertInGraveyard(player2, "Ornery Goblin");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("A token Midnight Reaper still triggers for another nontoken creature")
    void tokenMidnightReaperWatchesNontokenCreature() {
        Card drawnCard = new DirectCurrent();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        Card tokenReaper = new MidnightReaper();
        tokenReaper.setToken(true);
        harness.addToBattlefield(player1, tokenReaper);
        harness.addToBattlefield(player1, new OrneryGoblin());

        killCreatureWithDirectCurrent(player2, player1, "Ornery Goblin");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Simultaneous deaths trigger once for Reaper and each other nontoken creature")
    void simultaneousDeaths() {
        Card firstDraw = new DirectCurrent();
        Card secondDraw = new DirectCurrent();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MidnightReaper());
        harness.addToBattlefield(player1, new OrneryGoblin());
        harness.addToBattlefield(player2, new OrneryGoblin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new RitualOfSoot(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Midnight Reaper");
        harness.assertInGraveyard(player1, "Ornery Goblin");
        harness.assertInGraveyard(player2, "Ornery Goblin");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    private void killCreatureWithDirectCurrent(Player caster, Player targetController,
                                               String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(caster, List.of(new DirectCurrent()));
        harness.addMana(caster, ManaColor.RED, 2);

        harness.addMana(caster, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
