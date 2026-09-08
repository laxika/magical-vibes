package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ParkHeightsPegasus.class, GrizzlyBears.class})
class ParkHeightsPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card after two creatures enter under its controller's control")
    void drawsAfterTwoCreaturesEnterUnderYourControl() {
        addAttackingPegasus();
        recordEntered(player1, new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw after fewer than two creatures enter under its controller's control")
    void doesNotDrawAfterFewerThanTwoCreaturesEnter() {
        addAttackingPegasus();
        recordEntered(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Counts only creatures that entered under its controller's control")
    void ignoresCreatureEntriesUnderAnOpponentControl() {
        addAttackingPegasus();
        recordEntered(player1, new GrizzlyBears());
        recordEntered(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Checks the creature-entry condition when the trigger resolves")
    void checksConditionOnResolution() {
        addAttackingPegasus();
        recordEntered(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        recordEntered(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addAttackingPegasus() {
        Permanent pegasus = addCreatureReady(player1, new ParkHeightsPegasus());
        pegasus.setAttacking(true);
        return pegasus;
    }

    private void recordEntered(Player player, Card... cards) {
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player.getId(), ignored -> new ArrayList<>())
                .addAll(List.of(cards));
    }
}
