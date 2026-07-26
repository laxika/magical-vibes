package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AshesOfTheAbhorrent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImmortalCoil;
import com.github.laxika.magicalvibes.cards.m.MortalCombat;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LichsMirrorTest extends BaseCardTest {

    private static List<Card> shocks(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }

    private static List<Card> bears(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== The replacement fires for each replaceable loss =====

    @Test
    @DisplayName("Would-lose from lethal damage is replaced by the reset instead of ending the game")
    void resetsInsteadOfLosingFromLethalDamage() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setHand(player1, shocks(2));
        harness.setGraveyard(player1, shocks(1));
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        // The game continues and the player is reset rather than losing.
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerLifeTotals.get(p1)).isEqualTo(20);
        // Hand, graveyard, and all owned permanents were shuffled into the library, then 7 drawn.
        assertThat(gd.playerHands.get(p1)).hasSize(7);
        assertThat(gd.playerGraveyards.get(p1)).isEmpty();
        assertThat(gd.playerBattlefields.get(p1)).isEmpty();
        // 10 library + 2 hand + 1 graveyard + 1 Lich's Mirror = 14, minus 7 drawn = 7 left.
        assertThat(gd.playerDecks.get(p1)).hasSize(7);
    }

    @Test
    @DisplayName("Would-lose from an empty-library draw is replaced by the reset")
    void resetsInsteadOfLosingFromEmptyLibraryDraw() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setHand(player1, shocks(10));
        harness.setLibrary(player1, List.of());
        gd.playersAttemptedDrawFromEmptyLibrary.add(p1);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(p1)).hasSize(7);
        // The replaced draw attempt must not still be pending, or the next check would end the game.
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).doesNotContain(p1);
    }

    @Test
    @DisplayName("Would-lose from a 'you lose the game' ability is replaced by the reset")
    void resetsInsteadOfLosingFromLoseTheGameAbility() {
        UUID p1 = player1.getId();
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ImmortalCoil());
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, shocks(10));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.runStateBasedActions(); // Immortal Coil's state trigger goes on the stack
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();   // resolve it

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(p1)).hasSize(7);
        assertThat(gd.playerBattlefields.get(p1)).isEmpty();
    }

    @Test
    @DisplayName("Poison loss is replaced, but poison is not reset so the next check ends the game")
    void poisonLossIsReplacedOnceButPoisonRemains() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setLibrary(player1, shocks(10));
        gd.playerPoisonCounters.put(p1, 10);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        // The ruling is explicit that Lich's Mirror does not remove poison counters.
        assertThat(gd.playerPoisonCounters.get(p1)).isEqualTo(10);

        // The Mirror shuffled itself away, so the still-lethal poison finishes the game.
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    // ===== What the replacement must NOT do =====

    @Test
    @DisplayName("Without Lich's Mirror the player loses normally at 0 life")
    void losesNormallyWithoutMirror() {
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Lich's Mirror shuffles itself away and can't save the same player twice")
    void doesNotSaveTwice() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        // The Mirror is a permanent the player owns, so it was shuffled into the library.
        assertThat(gd.playerBattlefields.get(p1)).isEmpty();

        // A second lethal state now finishes the game — the Mirror is gone.
        harness.setLife(player1, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does nothing when the controller can't lose the game at all")
    void doesNothingWhenControllerCannotLose() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player1, new LichsMirror());
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        // "If you can't lose the game (for example, you control a Platinum Angel), Lich's Mirror
        // won't do anything" — there is no loss to replace, so no reset happens.
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerLifeTotals.get(p1)).isZero();
        assertThat(gd.playerBattlefields.get(p1)).hasSize(2);
        assertThat(gd.playerDecks.get(p1)).hasSize(10);
    }

    @Test
    @DisplayName("An opponent's 'wins the game' effect is not a replaceable loss")
    void doesNotReplaceAnOpponentWinningTheGame() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setLibrary(player1, shocks(10));
        harness.addToBattlefield(player2, new MortalCombat());
        harness.setGraveyard(player2, bears(20));

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve Mortal Combat's trigger

        // "Lich's Mirror has no effect if a spell or ability … states that a player 'wins the
        // game.' If a player wins the game, the game ends immediately." The Mirror stays put.
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(p1)).hasSize(1);
    }

    // ===== Details of the reset itself =====

    @Test
    @DisplayName("A draw that can't be completed re-arms the empty-library loss")
    void shortLibraryStillLosesAtTheNextCheck() {
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        // The Mirror itself is the only card available, so six of the seven draws fail: "you'll be
        // unable to complete at least one of those draws and you'll lose the game the next time
        // state-based actions are checked."
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Life total stays put when the controller can't gain life, so they still lose")
    void lifeTotalIsNotForcedWhenControllerCannotGainLife() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player2, new LeylineOfPunishment()); // players can't gain life
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        // The total *becomes* 20 by gaining that much life (CR 119.5), so a player who can't gain
        // life keeps their old total: "it stays at whatever it is rather than becoming 20".
        assertThat(gd.playerLifeTotals.get(p1)).isZero();
        assertThat(gd.playerHands.get(p1)).hasSize(7);

        // The reset still happened, but the player is still at 0 and the Mirror is gone.
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Owned tokens leave the battlefield without dying")
    void ownedTokensLeaveWithoutDying() {
        UUID p1 = player1.getId();
        Card token = new GrizzlyBears();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        harness.addToBattlefield(player1, new LichsMirror());
        // "Whenever a creature dies, you gain 1 life" — watching from the other side of the board.
        harness.addToBattlefield(player2, new AshesOfTheAbhorrent());
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        // Tokens are shuffled into the library, which is a battlefield-to-library move, not a
        // death — so no dies trigger fires.
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Ashes of the Abhorrent"));
        // ...and the token ceases to exist rather than staying somewhere it could be drawn.
        assertThat(gd.playerDecks.get(p1)).noneMatch(Card::isToken);
        assertThat(gd.playerHands.get(p1)).noneMatch(Card::isToken);
        assertThat(gd.playerGraveyards.get(p1)).isEmpty();
    }

    @Test
    @DisplayName("Emptying the graveyard into the library notifies graveyard-departure watchers")
    void notifiesThatCardsLeftTheGraveyard() {
        UUID p1 = player1.getId();
        harness.addToBattlefield(player1, new LichsMirror());
        harness.setGraveyard(player1, shocks(3));
        harness.setLibrary(player1, shocks(10));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(p1)).isEmpty();
        // "If one or more cards left your graveyard this turn" must see the reset.
        assertThat(gd.playersWhoseCardsLeftGraveyardThisTurn).contains(p1);
    }
}
