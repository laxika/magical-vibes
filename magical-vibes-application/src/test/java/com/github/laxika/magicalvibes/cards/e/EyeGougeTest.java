package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HulkingCyclops;
import com.github.laxika.magicalvibes.cards.f.Forest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeGouge.class, GrizzlyBears.class, HulkingCyclops.class, Forest.class})
class EyeGougeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a non-Cyclops creature -1/-1 without destroying it")
    void debuffsNonCyclopsWithoutDestroyingIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        castEyeGouge(target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a Cyclops after giving it -1/-1")
    void destroysCyclops() {
        harness.addToBattlefield(player2, new HulkingCyclops());
        UUID targetId = harness.getPermanentId(player2, "Hulking Cyclops");

        castEyeGouge(targetId);

        harness.assertNotOnBattlefield(player2, "Hulking Cyclops");
        harness.assertInGraveyard(player2, "Hulking Cyclops");
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        castEyeGouge(target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new EyeGouge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castEyeGouge(UUID targetId) {
        harness.setHand(player1, List.of(new EyeGouge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
