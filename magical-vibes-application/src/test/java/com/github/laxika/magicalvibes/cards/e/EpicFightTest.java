package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({EpicFight.class, GrizzlyBears.class, HillGiant.class})
class EpicFightTest extends BaseCardTest {

    @Test
    @DisplayName("Doubling mode doubles a target creature's power and toughness")
    void doublingModeDoublesTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(new int[]{0}, List.of(creature.getId()));

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Doubling mode lasts until end of turn")
    void doublingModeExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(new int[]{0}, List.of(creature.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fight mode makes the two target creatures fight")
    void fightModeMakesCreaturesFight() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{1}, List.of(ownCreature.getId(), opposingCreature.getId()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both modes resolve and may share the creature target")
    void bothModesResolveWithSharedTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(new int[]{0, 1}, List.of(ownCreature.getId(), ownCreature.getId(), opposingCreature.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Fight mode requires its first target to be controlled")
    void fightModeRequiresControlledFirstTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{1}, List.of(opposingCreature.getId(), ownCreature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        prepareCard();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new EpicFight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
