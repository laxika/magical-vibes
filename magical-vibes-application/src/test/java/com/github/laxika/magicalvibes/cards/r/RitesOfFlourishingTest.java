package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RitesOfFlourishing.class, Forest.class, Naturalize.class})
class RitesOfFlourishingTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Controller draws one additional card during their draw step")
    void triggersDrawForController() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Opponent draws one additional card during their own draw step, controller draws nothing")
    void triggersDrawForOpponentOnly() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 2);
    }

    @Test
    @DisplayName("Each player may play one additional land per turn")
    void raisesLandPlayLimitForEachPlayer() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Each player can use the additional land-play permission")
    void allowsTwoLandPlays() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(2);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Two copies stack for both the extra draw and the extra land play")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());
        harness.addToBattlefield(player2, new RitesOfFlourishing());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(3);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("Opponent can play two lands on their own turn")
    void opponentCanPlayTwoLands() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player2, 0);
        harness.playLand(player2, 0);

        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Removing the enchantment does not stop its pending draw trigger")
    void pendingDrawSurvivesSourceRemoval() {
        var rites = harness.addToBattlefieldAndReturn(player1, new RitesOfFlourishing());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.castAndResolveInstant(player2, 0, rites.getId());
        harness.assertNotOnBattlefield(player1, "Rites of Flourishing");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Removing the enchantment revokes its extra land permission immediately")
    void sourceRemovalRevokesExtraLandPlay() {
        var rites = harness.addToBattlefieldAndReturn(player1, new RitesOfFlourishing());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.playLand(player1, 0);

        harness.castAndResolveInstant(player2, 0, rites.getId());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Skipping the starting player's first draw step also skips the extra draw")
    void skippedFirstDrawStepDoesNotTrigger() {
        harness.addToBattlefield(player1, new RitesOfFlourishing());
        gd.startingPlayerId = player1.getId();
        gd.turnNumber = 1;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }
}
