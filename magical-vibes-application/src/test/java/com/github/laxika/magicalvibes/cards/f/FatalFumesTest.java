package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FatalFumesTest extends BaseCardTest {

    private void setupOpponentBear() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FatalFumes()));
        harness.addMana(player1, ManaColor.BLACK, 4);
    }

    @Test
    @DisplayName("Resolving gives -4/-2 and kills a 2/2")
    void killsGrizzlyBears() {
        setupOpponentBear();
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fatal Fumes");
    }

    @Test
    @DisplayName("A surviving creature keeps -4/-2 until it wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FatalFumes()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setToughnessModifier(5);

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(-2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast with an invalid target")
    void cannotCastWithInvalidTarget() {
        setupOpponentBear();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }
}
