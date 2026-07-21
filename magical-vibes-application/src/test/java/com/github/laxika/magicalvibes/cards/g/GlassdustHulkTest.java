package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlassdustHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Another artifact you control entering gives Glassdust Hulk +1/+1 and makes it unblockable")
    void allyArtifactEnterBoostsAndUnblockable() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new GlassdustHulk());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell, artifact enters, two triggers onto stack
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        assertThat(hulk.getPowerModifier()).isEqualTo(1);
        assertThat(hulk.getToughnessModifier()).isEqualTo(1);
        assertThat(hulk.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The boost and unblockable wear off at end of turn")
    void boostAndUnblockableWearOffAtCleanup() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new GlassdustHulk());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(hulk.getPowerModifier()).isEqualTo(1);
        assertThat(hulk.isCantBeBlocked()).isTrue();

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(0);
        assertThat(hulk.getToughnessModifier()).isEqualTo(0);
        assertThat(hulk.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger Glassdust Hulk")
    void opponentArtifactEnterDoesNotTrigger() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new GlassdustHulk());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GlazeFiend()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(0);
        assertThat(hulk.isCantBeBlocked()).isFalse();
    }
}
