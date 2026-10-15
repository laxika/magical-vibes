package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Flunk.class, MahamotiDjinn.class, GrizzlyBears.class, FountainOfYouth.class})
class FlunkTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -X/-X based on its controller's hand size")
    void usesTargetControllersHandSize() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new Flunk()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCastingMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Floors the reduction at zero when the target controller has eight cards")
    void floorsAtZero() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new Flunk()));
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCastingMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Reduction wears off at end of turn")
    void reductionWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new Flunk()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCastingMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new MahamotiDjinn());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Flunk()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Counts hand size at resolution and fixes the reduction afterward")
    void countsHandAtResolution() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new Flunk()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCastingMana();

        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);

        harness.setHand(player2, List.of());

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Mahamoti Djinn");
    }

    @Test
    @DisplayName("Can target your creature and excludes Flunk from your hand count")
    void targetsOwnCreature() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new Flunk(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of());
        addCastingMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Mahamoti Djinn");
    }

    @Test
    @DisplayName("An empty hand gives -7/-7 and puts a creature with nonpositive toughness in the graveyard")
    void emptyHandKillsCreature() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new Flunk()));
        harness.setHand(player2, List.of());
        addCastingMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Mahamoti Djinn");
        harness.assertInGraveyard(player2, "Mahamoti Djinn");
    }

    @Test
    @DisplayName("Exactly seven cards gives no reduction")
    void sevenCardsGivesNoReduction() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new Flunk()));
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCastingMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Mahamoti Djinn");
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MahamotiDjinn());
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
