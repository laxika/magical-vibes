package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostlyPilferer.class, AncientGrudge.class, FountainOfYouth.class, GrizzlyBears.class})
class GhostlyPilfererTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} after it untaps draws a card")
    void payingAfterUntappingDrawsCard() {
        Permanent pilferer = addTappedPilferer(player1);
        runUntapStep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(pilferer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the untap payment draws no card")
    void decliningUntapPaymentDrawsNothing() {
        addTappedPilferer(player1);
        gd.skipNextDrawStepCount.put(player1.getId(), 1);

        runUntapStep(player1);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("An opponent casting a spell from their graveyard draws a card")
    void opponentCastingFromGraveyardDrawsCard() {
        harness.addToBattlefield(player1, new GhostlyPilferer());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player2, 0, harness.getPermanentId(player1, "Fountain of Youth"));
        resolveStack();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("An opponent casting a spell from hand does not draw a card")
    void opponentCastingFromHandDrawsNothing() {
        harness.addToBattlefield(player1, new GhostlyPilferer());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player2, new ArrayList<>(List.of(new AncientGrudge())));
        harness.addMana(player2, ManaColor.RED, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Fountain of Youth"));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Discarding a card makes Ghostly Pilferer unblockable until end of turn")
    void discardMakesItUnblockableUntilEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent pilferer = addCreatureReady(player1, new GhostlyPilferer());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(pilferer.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pilferer.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("An already untapped Pilferer does not trigger during the untap step")
    void alreadyUntappedDoesNotTrigger() {
        harness.addToBattlefield(player1, new GhostlyPilferer());

        runUntapStep(player1);

        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Casting your own spell from the graveyard does not draw a card")
    void controllerCastingFromGraveyardDrawsNothing() {
        harness.addToBattlefield(player1, new GhostlyPilferer());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveFlashback(player1, 0,
                harness.getPermanentId(player2, "Fountain of Youth"));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Discard is paid before resolution and requires neither tapping nor mana")
    void discardCostIsPaidBeforeResolutionWhileTappedAndSummoningSick() {
        Permanent pilferer = harness.addToBattlefieldAndReturn(player1, new GhostlyPilferer());
        pilferer.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(pilferer.isCantBeBlocked()).isFalse();

        harness.passBothPriorities();

        assertThat(pilferer.isCantBeBlocked()).isTrue();
        assertThat(pilferer.isTapped()).isTrue();
    }

    private Permanent addTappedPilferer(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GhostlyPilferer());
        permanent.setSummoningSick(false);
        permanent.tap();
        return permanent;
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(untappingPlayer, TurnStep.UPKEEP);
    }
    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
