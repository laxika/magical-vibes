package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Disorient.class, RuneclawBear.class})
class DisorientTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Disorient gives -7/-0 to target creature")
    void resolvesAndDebuffsTarget() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Disorient()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(-7);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(-5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Debuff from Disorient wears off at cleanup step")
    void debuffWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Disorient()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Disorient fizzles if target is removed")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Disorient()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castInstant(player1, 0, bearId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Disorient can target an opponent's creature without affecting other creatures")
    void debuffsOpponentsCreatureOnly() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Disorient()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, opposingBear.getId());

        assertThat(opposingBear.getEffectivePower()).isEqualTo(-5);
        assertThat(opposingBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Two Disorients apply cumulative power reductions until cleanup")
    void multipleDisorientsStackAndExpire() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Disorient(), new Disorient()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getEffectivePower()).isEqualTo(-12);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Runeclaw Bear");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }
}
