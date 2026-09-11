package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FontOfAgonies;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.p.Prosperity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZursWeirding.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class ZursWeirdingTest extends BaseCardTest {
    @Test
    @DisplayName("Opponent pays 2 life to send the drawn card to its owner's graveyard")
    void opponentPaysLifeToDenyDraw() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // The other player (player2) is asked whether to pay 2 life.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the payment lets the drawing player draw the revealed card")
    void decliningLetsPlayerDraw() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draw happens normally when the other player can't pay 2 life")
    void drawsNormallyWhenOpponentCannotPay() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setLife(player2, 1);

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // No payment is possible — the drawing player simply draws, opponent's life is untouched.
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("Opponent may pay 2 life to prevent an empty-library draw")
    void opponentMayPreventEmptyLibraryDraw() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setLibrary(player1, List.of());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Declining an empty-library replacement makes the drawing player lose")
    void decliningEmptyLibraryDrawCausesLoss() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setLibrary(player1, List.of());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("A second Zur's Weirding can replace the draw after the first payment is declined")
    void multipleCopiesCanReplaceTheSameDraw() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed(Prosperity.class)
    void multiCardDrawIsReplacedOneCardAtATime() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setHand(player1, List.of(new Prosperity()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player2, List.of(new AirElemental(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(player1, 0, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @CardUsed(FontOfAgonies.class)
    @DisplayName("Paying to deny a draw triggers effects that watch for life payments")
    void paymentTriggersLifePaymentAbilities() {
        harness.addToBattlefield(player1, new ZursWeirding());
        var font = harness.addToBattlefieldAndReturn(player2, new FontOfAgonies());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(2);
    }

    @Test
    @CardUsed(PlatinumEmperion.class)
    @DisplayName("A player whose life total cannot change cannot pay to deny a draw")
    void cannotPayWhenLifeTotalCannotChange() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Grizzly Bears");
    }
    @Test
    @DisplayName("Both players see each other's hands while Zur's Weirding is on the battlefield")
    void bothHandsRevealed() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.passPriority(player1);

        // Controller of Zur's Weirding sees the opponent's hand.
        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Grizzly Bears"));

        // The opponent also sees the controller's hand (unlike Telepathy).
        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\"") && m.contains("Air Elemental"));
    }

    @Test
    @DisplayName("Hands are no longer revealed after Zur's Weirding leaves the battlefield")
    void handsHiddenAfterRemoval() {
        harness.addToBattlefield(player1, new ZursWeirding());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.passPriority(player1);

        List<String> p2Messages = harness.getConn2().getSentMessages();
        assertThat(p2Messages).anyMatch(m -> m.contains("\"opponentHand\":[]"));
        assertThat(p2Messages).noneMatch(m -> m.contains("\"opponentHand\"") && m.contains("Air Elemental"));
    }
}
