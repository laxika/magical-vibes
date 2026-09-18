package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({GhituAmplifier.class, GrizzlyBears.class, FountainOfYouth.class, Shock.class})
class GhituAmplifierTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, Ghitu Amplifier does not return a creature")
    void withoutKickerDoesNotReturnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhituAmplifier()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(target).isIn(gd.playerBattlefields.get(player2.getId()));
        harness.assertOnBattlefield(player1, "Ghitu Amplifier");
    }

    @Test
    @DisplayName("When kicked, Ghitu Amplifier returns a target creature an opponent controls")
    void kickedReturnsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhituAmplifier()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ghitu Amplifier");
    }

    @Test
    @DisplayName("The kicked ability cannot target a noncreature permanent")
    void kickedCannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GhituAmplifier()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Casting an instant boosts Ghitu Amplifier until end of turn")
    void instantBoostsUntilEndOfTurn() {
        Permanent amplifier = addAmplifier();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(amplifier.getPowerModifier()).isEqualTo(2);
        assertThat(amplifier.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(amplifier.getPowerModifier()).isZero();
        assertThat(amplifier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Casting a creature does not boost Ghitu Amplifier")
    void creatureDoesNotBoost() {
        Permanent amplifier = addAmplifier();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(amplifier.getPowerModifier()).isZero();
        assertThat(amplifier.getToughnessModifier()).isZero();
    }

    private Permanent addAmplifier() {
        harness.addToBattlefield(player1, new GhituAmplifier());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Ghitu Amplifier");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
