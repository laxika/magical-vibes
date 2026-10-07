package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BruteSuit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldenTailDisciple;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuitUp.class, BruteSuit.class, NetworkTerminal.class, Forest.class, GoldenTailDisciple.class})
class SuitUpTest extends BaseCardTest {

    @Test
    void makesCreatureAnArtifactCreatureWithBasePowerAndToughnessFourFiveAndDraws() {
        Permanent target = addCreatureReady(player2, new GoldenTailDisciple());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new SuitUp()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void makesVehicleAnArtifactCreatureWithBasePowerAndToughnessFourFive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BruteSuit());
        harness.setHand(player1, List.of(new SuitUp()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void effectWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GoldenTailDisciple());
        harness.setHand(player1, List.of(new SuitUp()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void cannotTargetNonCreatureNonVehiclePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        harness.setHand(player1, List.of(new SuitUp()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    void fizzlingFromMissingTargetDoesNotDraw() {
        Permanent target = addCreatureReady(player2, new GoldenTailDisciple());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new SuitUp()));
        addMana();

        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void retainsExistingEnchantmentTypeAndLifelink() {
        Permanent target = addCreatureReady(player1, new GoldenTailDisciple());
        harness.setHand(player1, List.of(new SuitUp()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isEnchantment(gd, target)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void countersStillModifyTheNewBasePowerAndToughness() {
        Permanent target = addCreatureReady(player1, new GoldenTailDisciple());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new SuitUp()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    void vehicleRemainsAnimatedDuringEndStepAndRevertsAfterCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BruteSuit());
        harness.setHand(player1, List.of(new SuitUp()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, target)).isFalse();
        assertThat(gqs.isArtifact(gd, target)).isTrue();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
