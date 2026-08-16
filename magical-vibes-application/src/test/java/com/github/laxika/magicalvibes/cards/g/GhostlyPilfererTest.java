package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhostlyPilfererTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} after it untaps draws a card")
    void payingAfterUntappingDrawsCard() {
        Permanent pilferer = addTappedPilferer(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        runUntapStep(player1);
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

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Fountain of Youth"));
        resolveStack();

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

    private Permanent addTappedPilferer(Player player) {
        Permanent permanent = new Permanent(new GhostlyPilferer());
        permanent.setSummoningSick(false);
        permanent.tap();
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
