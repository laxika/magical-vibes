package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.i.IsochronScepter;
import com.github.laxika.magicalvibes.cards.p.Pongify;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NullProfusion.class, AvenRiftwatcher.class, UrborgTombOfYawgmoth.class,
        Spellbook.class, IsochronScepter.class, Pongify.class})
class NullProfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        harness.addToBattlefield(player1, new NullProfusion());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Casting a spell draws a card")
    void castingSpellDraws() {
        harness.addToBattlefield(player1, new NullProfusion());
        harness.castFromHand(player1, new AvenRiftwatcher(), "{2}{W}");

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Playing a land draws a card")
    void playingLandDraws() {
        harness.addToBattlefield(player1, new NullProfusion());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, new ArrayList<>(List.of(new UrborgTombOfYawgmoth())));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        harness.assertOnBattlefield(player1, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Opponent casting a spell does not trigger the draw")
    void opponentSpellDoesNotDraw() {
        harness.addToBattlefield(player1, new NullProfusion());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new AvenRiftwatcher(), "{2}{W}");

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Controller must discard down to two during cleanup")
    void controllerDiscardsDownToTwo() {
        harness.addToBattlefield(player1, new NullProfusion());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player1, new ArrayList<>(List.of(
                new AvenRiftwatcher(), new AvenRiftwatcher(),
                new AvenRiftwatcher(), new AvenRiftwatcher()
        )));

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's maximum hand size is unaffected")
    void opponentHandSizeUnaffected() {
        harness.addToBattlefield(player1, new NullProfusion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player2, new ArrayList<>(List.of(
                new AvenRiftwatcher(), new AvenRiftwatcher(), new AvenRiftwatcher(),
                new AvenRiftwatcher(), new AvenRiftwatcher(), new AvenRiftwatcher()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("The draw step itself is skipped, including its priority window")
    void drawStepDoesNotOccur() {
        harness.addToBattlefield(player1, new NullProfusion());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        harness.withAutoStop(TurnStep.DRAW, () ->
                harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Opponent still draws during their draw step")
    void opponentDrawStepUnaffected() {
        harness.addToBattlefield(player1, new NullProfusion());
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.setLibrary(player2, List.of(new AvenRiftwatcher()));
        harness.setHand(player2, List.of());

        harness.passUntil(player2, TurnStep.DRAW);

        harness.assertInHand(player2, "Aven Riftwatcher");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent playing a land does not trigger a draw")
    void opponentLandDoesNotDraw() {
        harness.addToBattlefield(player1, new NullProfusion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new UrborgTombOfYawgmoth()));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        harness.assertOnBattlefield(player2, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Putting a land onto the battlefield is not playing a card")
    void puttingLandOntoBattlefieldDoesNotDraw() {
        harness.addToBattlefield(player1, new NullProfusion());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player1, new UrborgTombOfYawgmoth());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Later Null Profusion overrides an earlier Spellbook")
    void laterNullProfusionSetsHandSizeToTwo() {
        harness.enterBattlefieldAndReturn(player1, new Spellbook());
        harness.enterBattlefieldAndReturn(player1, new NullProfusion());
        harness.setHand(player1, List.of(
                new AvenRiftwatcher(), new AvenRiftwatcher(), new AvenRiftwatcher()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Later Spellbook overrides Null Profusion's maximum hand size")
    void laterSpellbookRemovesHandSizeLimit() {
        harness.enterBattlefieldAndReturn(player1, new NullProfusion());
        harness.enterBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(
                new AvenRiftwatcher(), new AvenRiftwatcher(), new AvenRiftwatcher()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Casting a copy of a card with Isochron Scepter does not draw")
    void castingCardCopyDoesNotDraw() {
        IsochronScepter scepterCard = new IsochronScepter();
        Pongify imprint = new Pongify();
        gd.setImprintedCard(scepterCard, imprint);
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, scepterCard);
        gd.exiledCards.add(new ExiledCardEntry(imprint, player1.getId(), scepterCard.getId()));
        scepter.setSummoningSick(false);
        harness.addToBattlefield(player1, new NullProfusion());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AvenRiftwatcher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        harness.assertNotOnBattlefield(player2, "Aven Riftwatcher");
        harness.assertOnBattlefield(player2, "Ape");
    }
}
