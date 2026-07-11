package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindslicerTest extends BaseCardTest {

    @Test
    @DisplayName("When Mindslicer dies, its death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new Mindslicer());

        setupCombatWhereMindslicerDies();
        harness.passBothPriorities(); // Combat damage — Mindslicer dies

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mindslicer"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mindslicer");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each player discard their entire hand")
    void eachPlayerDiscardsEntireHand() {
        harness.addToBattlefield(player1, new Mindslicer());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        setupCombatWhereMindslicerDies();
        harness.passBothPriorities(); // Combat damage — Mindslicer dies
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears")).hasSize(3);
    }

    @Test
    @DisplayName("Death trigger empties a non-empty hand while logging the empty one")
    void handlesEmptyHand() {
        harness.addToBattlefield(player1, new Mindslicer());
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        setupCombatWhereMindslicerDies();
        harness.passBothPriorities(); // Combat damage — Mindslicer dies
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));
    }

    // ===== Helpers =====

    private void setupCombatWhereMindslicerDies() {
        Permanent mindslicerPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mindslicer"))
                .findFirst().orElseThrow();
        mindslicerPerm.setSummoningSick(false);
        mindslicerPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
