package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
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

class FleetingDistractionTest extends BaseCardTest {

    private void setupBearAndSpell() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FleetingDistraction()));
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Test
    @DisplayName("Resolving gives -1/-0 to target creature")
    void resolvingGivesMinusOneMinusZero() {
        setupBearAndSpell();
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving draws a card for the caster")
    void resolvingDrawsACard() {
        setupBearAndSpell();
        harness.getGameData().playerDecks.get(player1.getId()).add(new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Debuff wears off at end of turn")
    void debuffWearsOff() {
        setupBearAndSpell();
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast with an invalid target")
    void cannotCastWithInvalidTarget() {
        setupBearAndSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }
}
