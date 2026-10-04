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
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Targeting yourself boosts and untaps the Guardian too")
    void targetingYourselfIncludesGuardian() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GreatOakGuardian());
        existing.tap();

        castGreatOakGuardian(player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        for (Permanent creature : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
            assertThat(creature.isTapped()).isFalse();
        }
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void laterCreaturesAreNotBoosted() {
        Permanent existing = harness.addToBattlefieldAndReturn(player2, new GreatOakGuardian());

        castGreatOakGuardian(player2.getId());
        Permanent later = harness.addToBattlefieldAndReturn(player2, new GreatOakGuardian());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(5);
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's end step")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        castGreatOakGuardian(player1.getId());

        Permanent guardian = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(7);
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
