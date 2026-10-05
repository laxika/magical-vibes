package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.m.MuseumNightwatch;
import com.github.laxika.magicalvibes.cards.u.UndercoverCrocodelf;
import com.github.laxika.magicalvibes.cards.m.MarketwatchPhantom;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProjektorInspector.class, Forest.class, MuseumNightwatch.class, MarketwatchPhantom.class, UndercoverCrocodelf.class, Conspiracy.class})
class ProjektorInspectorTest extends BaseCardTest {

    @Test
    void detectiveEnteringMayDrawThenDiscard() {
        Forest forest = new Forest();
        MuseumNightwatch nightwatch = new MuseumNightwatch();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new ProjektorInspector(), nightwatch));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nightwatch);
    }

    @Test
    void nonDetectiveEnteringDoesNotTrigger() {
        addCreatureReady(player1, new ProjektorInspector());
        harness.setHand(player1, List.of(new MuseumNightwatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void detectiveTurnedFaceUpMayDrawThenDiscard() {
        UndercoverCrocodelf detective = new UndercoverCrocodelf();
        harness.setHand(player1, List.of(detective));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent faceDownDetective = findPermanent(player1, "Undercover Crocodelf");
        addCreatureReady(player1, new ProjektorInspector());
        Forest forest = new Forest();
        MuseumNightwatch nightwatch = new MuseumNightwatch();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(nightwatch));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(faceDownDetective));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(faceDownDetective.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nightwatch);
    }

    @Test
    void mayDeclineWithoutDrawingOrDiscarding() {
        Forest draw = new Forest();
        MuseumNightwatch kept = new MuseumNightwatch();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new ProjektorInspector(), kept));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void anotherDetectiveEnteringTriggersAndCanDiscardTheDrawnCard() {
        addCreatureReady(player1, new ProjektorInspector());
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new MarketwatchPhantom()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void opponentDetectiveEnteringDoesNotTriggerInspector() {
        addCreatureReady(player1, new ProjektorInspector());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MarketwatchPhantom()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void faceDownDetectiveEnteringDoesNotTrigger() {
        addCreatureReady(player1, new ProjektorInspector());
        harness.setHand(player1, List.of(new UndercoverCrocodelf()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void nonDetectiveTurningFaceUpDoesNotTrigger() {
        harness.setHand(player1, List.of(new MuseumNightwatch()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        addCreatureReady(player1, new ProjektorInspector());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.turnFaceUp(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ownEntryTriggersEvenWhenConspiracyReplacesDetectiveType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(new ProjektorInspector()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }
}
