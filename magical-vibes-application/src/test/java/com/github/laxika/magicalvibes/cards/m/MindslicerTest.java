package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindslicer.class, DuskImp.class})
class MindslicerTest extends BaseCardTest {

    @Test
    @DisplayName("When Mindslicer dies, its death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        setupCombatWhereMindslicerDies();
        resolveCombat();

        harness.assertInGraveyard(player1, "Mindslicer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mindslicer");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each player discard their entire hand")
    void eachPlayerDiscardsEntireHand() {
        harness.setHand(player1, List.of(new DuskImp(), new DuskImp()));
        harness.setHand(player2, List.of(new DuskImp(), new DuskImp(), new DuskImp()));

        setupCombatWhereMindslicerDies();
        resolveCombat();
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Dusk Imp")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Dusk Imp")).hasSize(3);
    }

    @Test
    @DisplayName("Death trigger empties a non-empty hand while logging the empty one")
    void handlesEmptyHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new DuskImp()));

        setupCombatWhereMindslicerDies();
        resolveCombat();
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Dusk Imp");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    private void setupCombatWhereMindslicerDies() {
        Permanent mindslicerPerm = addCreatureReady(player1, new Mindslicer());
        mindslicerPerm.setAttacking(true);

        DuskImp bigBear = new DuskImp();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        Permanent blockerPerm = addCreatureReady(player2, bigBear);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
    }
}
