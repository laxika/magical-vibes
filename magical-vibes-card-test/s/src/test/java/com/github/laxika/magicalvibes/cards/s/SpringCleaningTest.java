package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HoofprintsOfTheStag;
import com.github.laxika.magicalvibes.cards.m.MilitiasPride;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpringCleaning.class, HoofprintsOfTheStag.class, MilitiasPride.class, Forest.class, DeeptreadMerrow.class})
class SpringCleaningTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SpringCleaning()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // {1}{G}
    }

    // Caster wins: their revealed top card (Deeptread Merrow, MV 2) beats the opponent's (Forest, MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new DeeptreadMerrow(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new DeeptreadMerrow(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash destroys the target and all other enchantments opponents control")
    void wonClashDestroysAllOpponentEnchantments() {
        prepare();
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());   // target enchantment
        harness.addToBattlefield(player2, new MilitiasPride()); // other opponent enchantment
        harness.addToBattlefield(player1, new HoofprintsOfTheStag());   // own enchantment, must survive
        stackClashWinForCaster();

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castAndResolveInstant(player1, 0, targetId);
        keepRevealedCards();

        harness.assertNotOnBattlefield(player2, "Hoofprints of the Stag");
        harness.assertNotOnBattlefield(player2, "Militia's Pride");
        // The caster's own enchantment is untouched.
        harness.assertOnBattlefield(player1, "Hoofprints of the Stag");
    }

    @Test
    @DisplayName("Losing the clash destroys only the target enchantment")
    void lostClashDestroysOnlyTarget() {
        prepare();
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());   // target enchantment
        harness.addToBattlefield(player2, new MilitiasPride()); // survives on a loss
        stackClashLossForCaster();

        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castAndResolveInstant(player1, 0, targetId);
        keepRevealedCards();

        harness.assertNotOnBattlefield(player2, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player2, "Militia's Pride");
        harness.assertInGraveyard(player2, "Hoofprints of the Stag");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        prepare();
        harness.addToBattlefield(player2, new DeeptreadMerrow());

        UUID creatureId = harness.getPermanentId(player2, "Deeptread Merrow");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void keepRevealedCards() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("A tied clash destroys only the target")
    void tiedClashDestroysOnlyTarget() {
        prepare();
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.addToBattlefield(player2, new MilitiasPride());
        harness.setLibrary(player1, List.of(new DeeptreadMerrow(), new Forest()));
        harness.setLibrary(player2, List.of(new DeeptreadMerrow(), new Forest()));

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Hoofprints of the Stag"));
        keepRevealedCards();

        harness.assertNotOnBattlefield(player2, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player2, "Militia's Pride");
    }

    @Test
    @DisplayName("Targeting your own enchantment still destroys opposing enchantments on a win")
    void canTargetOwnEnchantment() {
        prepare();
        harness.addToBattlefield(player1, new HoofprintsOfTheStag());
        harness.addToBattlefield(player1, new MilitiasPride());
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.addToBattlefield(player2, new DeeptreadMerrow());
        stackClashWinForCaster();

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Hoofprints of the Stag"));
        keepRevealedCards();

        harness.assertNotOnBattlefield(player1, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player1, "Militia's Pride");
        harness.assertNotOnBattlefield(player2, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player2, "Deeptread Merrow");
    }

    @Test
    @DisplayName("The active opponent chooses clash placement before the nonactive caster")
    void activeOpponentChoosesPlacementFirst() {
        prepare();
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        stackClashWinForCaster();

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Hoofprints of the Stag"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(((PendingInteraction.Scry) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("An illegal sole target prevents the clash and the enchantment sweep")
    void illegalTargetPreventsClash() {
        prepare();
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.addToBattlefield(player2, new MilitiasPride());
        stackClashWinForCaster();
        UUID targetId = harness.getPermanentId(player2, "Hoofprints of the Stag");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Militia's Pride");
        harness.assertInGraveyard(player1, "Spring Cleaning");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Bottoming both revealed cards does not change the already determined clash winner")
    void bottomingCardsPreservesClashWinner() {
        prepare();
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        harness.addToBattlefield(player2, new MilitiasPride());
        DeeptreadMerrow casterTop = new DeeptreadMerrow();
        Forest casterNext = new Forest();
        Forest opponentTop = new Forest();
        DeeptreadMerrow opponentNext = new DeeptreadMerrow();
        harness.setLibrary(player1, List.of(casterTop, casterNext));
        harness.setLibrary(player2, List.of(opponentTop, opponentNext));

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Hoofprints of the Stag"));
        harness.assertNotOnBattlefield(player2, "Hoofprints of the Stag");
        harness.assertOnBattlefield(player2, "Militia's Pride");
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(casterNext, casterTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentNext, opponentTop);
        harness.assertNotOnBattlefield(player2, "Militia's Pride");
    }

    @Test
    @DisplayName("Clash cards stay in place until both players have chosen their placement")
    void placementsWaitForBothDecisions() {
        prepare();
        harness.addToBattlefield(player2, new HoofprintsOfTheStag());
        DeeptreadMerrow casterTop = new DeeptreadMerrow();
        Forest casterNext = new Forest();
        Forest opponentTop = new Forest();
        Forest opponentNext = new Forest();
        harness.setLibrary(player1, List.of(casterTop, casterNext));
        harness.setLibrary(player2, List.of(opponentTop, opponentNext));

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Hoofprints of the Stag"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(casterTop, casterNext);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop, opponentNext);
    }
}
