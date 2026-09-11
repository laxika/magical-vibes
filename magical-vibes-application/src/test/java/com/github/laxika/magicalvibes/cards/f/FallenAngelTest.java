package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallenAngel.class, GrizzlyBears.class, HowlingMine.class})
class FallenAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature to the ability gives Fallen Angel +2/+1")
    void resolvingAbilityBoostsAngel() {
        Permanent angel = addCreatureReady(player1, new FallenAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();

        harness.assertOnBattlefield(player1, "Fallen Angel");
        assertThat(angel.getPowerModifier()).isEqualTo(2);
        assertThat(angel.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability stacks when activated multiple times")
    void canActivateMultipleTimes() {
        Permanent angel = addCreatureReady(player1, new FallenAngel());
        Permanent firstBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstBears.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, secondBears.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(angel.getPowerModifier()).isEqualTo(4);
        assertThat(angel.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fallen Angel can sacrifice itself to its own ability")
    void canSacrificeItself() {
        Permanent angel = addCreatureReady(player1, new FallenAngel());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Fallen Angel");
        harness.assertInGraveyard(player1, "Fallen Angel");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Self-sacrifice ability resolves without boosting the departed Angel")
    void selfSacrificeAbilityResolvesWithoutBoost() {
        Permanent angel = addCreatureReady(player1, new FallenAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(angel.getPowerModifier()).isZero();
        assertThat(angel.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can sacrifice only a creature controlled by Fallen Angel's controller")
    void cannotSacrificeOpponentCreature() {
        Permanent angel = addCreatureReady(player1, new FallenAngel());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, ownBears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(angel.getPowerModifier()).isEqualTo(2);
        assertThat(angel.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can sacrifice only a creature, not another permanent")
    void cannotSacrificeNoncreaturePermanent() {
        Permanent angel = addCreatureReady(player1, new FallenAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new HowlingMine());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, mine.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(angel.getPowerModifier()).isEqualTo(2);
        assertThat(angel.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent angel = addCreatureReady(player1, new FallenAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(angel.getPowerModifier()).isEqualTo(2);
        assertThat(angel.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.getPowerModifier()).isEqualTo(0);
        assertThat(angel.getToughnessModifier()).isEqualTo(0);
    }
}
