package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BounceOff;
import com.github.laxika.magicalvibes.cards.v.VoyagerGlidecar;
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

@CardUsed({HowlersHeavy.class, VoyagerGlidecar.class, BounceOff.class})
class HowlersHeavyTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling gives -3/-0 to a target opponent creature and draws a card")
    void cyclingDebuffsOpponentCreatureAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new HowlersHeavy());
        cycleHowlersHeavy(List.of(bears));

        assertThat(bears.getPowerModifier()).isEqualTo(-3);
        assertThat(bears.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Howler's Heavy");
        harness.assertInHand(player1, "Howler's Heavy");
    }

    @Test
    @DisplayName("Cycling can target a noncreature Vehicle")
    void cyclingDebuffsOpponentVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new VoyagerGlidecar());
        cycleHowlersHeavy(List.of(vehicle));

        assertThat(vehicle.getPowerModifier()).isEqualTo(-3);
        assertThat(vehicle.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cycling cannot target a creature you control")
    void cyclingCannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HowlersHeavy());
        harness.addToBattlefield(player2, new HowlersHeavy());
        harness.setHand(player1, List.of(new HowlersHeavy()));
        harness.setLibrary(player1, List.of(new HowlersHeavy()));
        addCyclingMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling with no legal target still draws a card")
    void cyclingWithNoLegalTargetStillDraws() {
        harness.setHand(player1, List.of(new HowlersHeavy()));
        harness.setLibrary(player1, List.of(new HowlersHeavy()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Howler's Heavy");
        harness.assertInHand(player1, "Howler's Heavy");
    }

    @Test
    @DisplayName("The -3/-0 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new HowlersHeavy());
        cycleHowlersHeavy(List.of(bears));

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The cycling trigger resolves before the separate cycling draw")
    void debuffResolvesBeforeDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HowlersHeavy());
        harness.setHand(player1, List.of(new HowlersHeavy()));
        harness.setLibrary(player1, List.of(new VoyagerGlidecar()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, creature.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(creature.getPowerModifier()).isEqualTo(-3);
        harness.assertNotInHand(player1, "Voyager Glidecar");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Voyager Glidecar");
    }

    @Test
    @DisplayName("Removing the cycling trigger's target does not prevent the cycling draw")
    void losingTargetStillDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HowlersHeavy());
        harness.setHand(player1, List.of(new HowlersHeavy()));
        harness.setLibrary(player1, List.of(new VoyagerGlidecar()));
        harness.setHand(player2, List.of(new BounceOff()));
        addCyclingMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, creature.getId());
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Howler's Heavy");
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInHand(player1, "Voyager Glidecar");
    }

    @Test
    @DisplayName("Cycling requires choosing a target for its mandatory trigger when one exists")
    void cannotSkipDebuffWithLegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HowlersHeavy());
        harness.setHand(player1, List.of(new HowlersHeavy()));
        harness.setLibrary(player1, List.of(new VoyagerGlidecar()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(creature.getPowerModifier()).isEqualTo(-3);
        harness.assertInHand(player1, "Voyager Glidecar");
    }

    private void cycleHowlersHeavy(List<Permanent> targets) {
        harness.setHand(player1, List.of(new HowlersHeavy()));
        harness.setLibrary(player1, List.of(new HowlersHeavy()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, targets.isEmpty() ? null : targets.getFirst().getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
