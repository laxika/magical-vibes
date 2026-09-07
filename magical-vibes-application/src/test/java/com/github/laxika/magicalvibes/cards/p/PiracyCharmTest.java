package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PiracyCharm.class, GrizzlyBears.class})
class PiracyCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 gives a target creature islandwalk until end of turn")
    void grantsIslandwalkUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.ISLANDWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Mode 1 gives a target creature +2/-1 until end of turn")
    void boostsTargetCreatureUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(1, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 2 makes a target player discard a card")
    void targetPlayerDiscards() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        cast(2, player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature modes reject a player target")
    void creatureModesRejectPlayerTarget() {
        harness.setHand(player1, List.of(new PiracyCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new PiracyCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castModalInstant(player1, 0, mode, List.of(targetId));
        harness.passBothPriorities();
    }
}
