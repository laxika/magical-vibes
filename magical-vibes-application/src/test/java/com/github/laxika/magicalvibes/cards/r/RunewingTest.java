package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RunewingTest extends BaseCardTest {

    @Test
    @DisplayName("Runewing dies from Wrath of God and its controller draws a card")
    void diesFromWrathDrawsCard() {
        harness.addToBattlefield(player1, new Runewing());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Runewing");
        harness.assertInGraveyard(player1, "Runewing");

        // Resolve the death trigger — mandatory draw, no prompt
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Runewing dies in combat and its controller draws a card")
    void diesInCombatDrawsCard() {
        Permanent runewing = new Permanent(new Runewing());
        runewing.setSummoningSick(false);
        runewing.setBlocking(true);
        runewing.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(runewing);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Runewing");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Runewing surviving combat does not draw a card")
    void survivesNoDraw() {
        Permanent runewing = new Permanent(new Runewing());
        runewing.setSummoningSick(false);
        runewing.setBlocking(true);
        runewing.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(runewing);

        GrizzlyBears weak = new GrizzlyBears();
        weak.setPower(1);
        weak.setToughness(1);
        Permanent attacker = new Permanent(weak);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Runewing");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
