package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmptyCityRuse.class, AlertShuInfantry.class, RelentlessAssault.class})
class EmptyCityRuseTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving flags the target opponent to skip their next combat phase")
    void resolvingFlagsTargetOpponent() {
        harness.setHand(player1, List.of(new EmptyCityRuse()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The flagged opponent jumps from precombat main straight to postcombat main")
    void flaggedOpponentSkipsCombat() {
        // Give player2 a ready attacker so combat would otherwise halt progression.
        addCreatureReady(player2, new AlertShuInfantry());

        gd.skipNextCombatPhaseCount.put(player2.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Skips an additional combat phase created during the affected turn")
    void skipsAdditionalCombatPhaseInAffectedTurn() {
        harness.setHand(player1, List.of(new EmptyCityRuse()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        harness.castFromHand(player2, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Cannot target self")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new EmptyCityRuse()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
