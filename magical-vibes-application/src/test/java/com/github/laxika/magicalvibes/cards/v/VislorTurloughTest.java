package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VislorTurlough.class, Forest.class})
class VislorTurloughTest extends BaseCardTest {

    @Test
    @DisplayName("May give control to an opponent and goads Vislor while they control it")
    void givesControlAndGoadsWhenAccepted() {
        castVislor();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vislor Turlough");
        harness.assertOnBattlefield(player2, "Vislor Turlough");
        Permanent vislor = findPermanent(player2, "Vislor Turlough");
        assertThat(als.getMustAttackRequirementCount(gd, vislor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the ETB may keeps control and does not goad Vislor")
    void doesNotGiveControlOrGoadWhenDeclined() {
        castVislor();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent vislor = findPermanent(player1, "Vislor Turlough");
        assertThat(als.getMustAttackRequirementCount(gd, vislor)).isZero();
    }

    @Test
    @DisplayName("Draws before losing life equal to the resulting hand size at its controller's end step")
    void drawsThenLosesLifeEqualToHandSize() {
        harness.addToBattlefield(player2, new VislorTurlough());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void castVislor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VislorTurlough()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The accepted control transfer gives Vislor the goaded designation")
    void transferredVislorIsGoaded() {
        castVislor();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.isGoaded(gd, findPermanent(player2, "Vislor Turlough"))).isTrue();
    }

    @Test
    @DisplayName("Goad requires attacking another opponent instead of the ability controller if able")
    void cannotAttackAbilityControllerWhenAnotherOpponentIsAvailable() {
        castVislor();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        Permanent vislor = findPermanent(player2, "Vislor Turlough");
        vislor.setSummoningSick(false);
        Player thirdPlayer = new Player(UUID.randomUUID(), "Charlie");
        UUID thirdPlayerId = thirdPlayer.getId();
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerNames.add("Charlie");
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerLifeTotals.put(thirdPlayerId, 20);

        assertThat(als.mustAttackOtherPlayerIfAble(gd, vislor, player1.getId())).isTrue();
        assertThat(als.mustAttackOtherPlayerIfAble(gd, vislor, thirdPlayerId)).isFalse();
    }

    @Test
    @DisplayName("After transfer the end-step ability affects the recipient, not the owner")
    void transferredVislorTriggersOnlyAtRecipientsEndStep() {
        castVislor();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(als.getMustAttackRequirementCount(gd,
                findPermanent(player2, "Vislor Turlough"))).isEqualTo(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
