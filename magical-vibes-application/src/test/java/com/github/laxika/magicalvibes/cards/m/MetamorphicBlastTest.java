package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OutlawMedic;
import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetamorphicBlast.class, GrizzlyBears.class, OutlawMedic.class, SterlingHound.class, MaskwoodNexus.class})
class MetamorphicBlastTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode makes a creature a white 0/1 Rabbit until end of turn")
    void transformsTargetCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()), 2);

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.RABBIT);
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).doesNotContain(CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The second mode makes the target player draw two cards")
    void targetPlayerDrawsTwoCards() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of());

        cast(new int[]{1}, List.of(player2.getId()), 4);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Both modes resolve and charge both additional costs")
    void bothModesResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of());

        cast(new int[]{0, 1}, List.of(target.getId(), player2.getId()), 5);

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.RABBIT);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The creature mode rejects a player target")
    void creatureModeRejectsPlayerTarget() {
        assertThatThrownBy(() -> cast(new int[]{0}, List.of(player2.getId()), 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The draw mode can target its controller")
    void controllerDrawsTwoCards() {
        harness.setLibrary(player1, List.of(new SterlingHound(), new SterlingHound()));

        cast(new int[]{1}, List.of(player1.getId()), 4);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Transformation preserves abilities and applies counters on top of base stats")
    void transformationPreservesLifelinkAndCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OutlawMedic());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(new int[]{0}, List.of(target.getId()), 2);

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.RABBIT);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("A transformed creature retains its death trigger")
    void transformationPreservesDeathTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OutlawMedic());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new SterlingHound()));

        cast(new int[]{0}, List.of(target.getId()), 2);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An artifact creature remains an artifact after becoming a Rabbit")
    void transformationPreservesArtifactType() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());

        cast(new int[]{0}, List.of(target.getId()), 2);

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.RABBIT);
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The player mode rejects a creature target")
    void drawModeRejectsCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(target.getId()), 4))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing both modes requires both additional costs")
    void bothModesRejectInsufficientMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());

        assertThatThrownBy(() -> cast(new int[]{0, 1}, List.of(target.getId(), player2.getId()), 4))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw mode still resolves when the creature target leaves the battlefield")
    void drawsWhenCreatureTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.setLibrary(player2, List.of(new SterlingHound(), new SterlingHound()));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MetamorphicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(target.getId(), player2.getId()));

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Transformation overrides an earlier Maskwood Nexus")
    void transformationOverridesEarlierCreatureTypeEffect() {
        harness.addToBattlefield(player2, new MaskwoodNexus());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());

        cast(new int[]{0}, List.of(target.getId()), 2);

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.RABBIT);
    }

    @Test
    @DisplayName("A later Maskwood Nexus gives the transformed creature every creature type")
    void laterCreatureTypeEffectOverridesTransformation() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        cast(new int[]{0}, List.of(target.getId()), 2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.RABBIT);

        harness.setHand(player1, List.of(new MaskwoodNexus()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .contains(CardSubtype.RABBIT, CardSubtype.DOG, CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int totalMana) {
        harness.setHand(player1, List.of(new MetamorphicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
        harness.passBothPriorities();
    }
}
