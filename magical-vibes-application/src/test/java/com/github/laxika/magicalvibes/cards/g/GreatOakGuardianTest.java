package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreatOakGuardian.class, GrizzlyBears.class})
class GreatOakGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts and untaps creatures controlled by the targeted player")
    void boostsAndUntapsTargetPlayersCreatures() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        targetCreature.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.tap();

        castGreatOakGuardian(player2.getId());

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(4);
        assertThat(targetCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The temporary boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGreatOakGuardian(player2.getId());
        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(2);
    }

    private void castGreatOakGuardian(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GreatOakGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
