package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UncomfortableChillTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures opponents control -2/-0 and leaves your own creatures alone")
    void weakensOnlyOpponentCreatures() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UncomfortableChill()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws a card")
    void drawsACard() {
        harness.setHand(player1, List.of(new UncomfortableChill()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The -2/-0 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UncomfortableChill()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(2);
    }
}
