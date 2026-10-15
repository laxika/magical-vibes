package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CentaurHealer;
import com.github.laxika.magicalvibes.cards.d.DramaticRescue;
import com.github.laxika.magicalvibes.cards.s.SellerOfSongbirds;
import com.github.laxika.magicalvibes.cards.s.SupremeVerdict;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Runewing.class, CentaurHealer.class, SellerOfSongbirds.class, SupremeVerdict.class, DramaticRescue.class})
class RunewingTest extends BaseCardTest {

    @Test
    @DisplayName("Runewing dies from Supreme Verdict and its controller draws a card")
    void diesFromVerdictDrawsCard() {
        harness.addToBattlefield(player1, new Runewing());

        harness.setHand(player1, List.of(new SupremeVerdict()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runewing");
        harness.assertInGraveyard(player1, "Runewing");

        // Resolve the death trigger — mandatory draw, no prompt
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Runewing dies in combat and its controller draws a card")
    void diesInCombatDrawsCard() {
        Permanent runewing = harness.addToBattlefieldAndReturn(player1, new Runewing());
        runewing.setSummoningSick(false);
        runewing.setBlocking(true);
        runewing.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new CentaurHealer());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runewing");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Runewing surviving combat does not draw a card")
    void survivesNoDraw() {
        Permanent runewing = harness.addToBattlefieldAndReturn(player1, new Runewing());
        runewing.setSummoningSick(false);
        runewing.setBlocking(true);
        runewing.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new SellerOfSongbirds());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runewing");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Returning Runewing to hand does not trigger a draw")
    void returningToHandDoesNotDraw() {
        Permanent runewing = harness.addToBattlefieldAndReturn(player1, new Runewing());
        harness.setHand(player1, List.of(new DramaticRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, runewing.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runewing");
        harness.assertNotInGraveyard(player1, "Runewing");
        harness.assertInHand(player1, "Runewing");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize);
        assertThat(gd.stack).isEmpty();
    }
}
