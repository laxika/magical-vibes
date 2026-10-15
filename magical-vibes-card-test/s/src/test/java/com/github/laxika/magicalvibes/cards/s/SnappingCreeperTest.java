package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TectonicEdge;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnappingCreeper.class, TectonicEdge.class})
class SnappingCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Snapping Creeper vigilance until end of turn")
    void landfallGrantsVigilance() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SnappingCreeper());
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Landfall vigilance wears off at end of turn")
    void landfallVigilanceWearsOff() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SnappingCreeper());
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's landfall does not grant vigilance")
    void opponentLandDoesNotTrigger() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SnappingCreeper());
        harness.setHand(player2, List.of(new TectonicEdge()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Landfall uses the stack before granting vigilance")
    void vigilanceWaitsForTriggerResolution() {
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new SnappingCreeper());
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, creeper, Keyword.VIGILANCE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creeper, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Landfall vigilance lets Snapping Creeper attack without tapping")
    void landfallAllowsUntappedAttack() {
        Permanent creeper = addCreatureReady(player1, new SnappingCreeper());
        harness.setHand(player1, List.of(new TectonicEdge()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(creeper.isAttacking()).isTrue();
        assertThat(creeper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Creeper entering after landfall does not gain vigilance")
    void laterCreeperDoesNotGainVigilance() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SnappingCreeper());
        harness.setHand(player1, List.of(new TectonicEdge()));
        harness.playLand(player1, 0);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new SnappingCreeper());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, later, Keyword.VIGILANCE)).isFalse();
    }
}
