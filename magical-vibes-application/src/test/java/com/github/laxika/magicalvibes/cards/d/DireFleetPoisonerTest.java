package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
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

class DireFleetPoisonerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an attacking Pirate +1/+1 and deathtouch")
    void etbBoostsAttackingPirateAndGrantsDeathtouch() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        pirate.setAttacking(true);
        castPoisoner(pirate.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pirate.getEffectivePower()).isEqualTo(3);
        assertThat(pirate.getEffectiveToughness()).isEqualTo(3);
        assertThat(pirate.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("ETB boost and deathtouch wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        pirate.setAttacking(true);
        castPoisoner(pirate.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pirate.getEffectivePower()).isEqualTo(2);
        assertThat(pirate.getEffectiveToughness()).isEqualTo(2);
        assertThat(pirate.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Cannot target a Pirate that is not attacking")
    void cannotTargetNonAttackingPirate() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        harness.setHand(player1, List.of(new DireFleetPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, pirate.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking Pirate you control");
    }

    @Test
    @DisplayName("Cannot target an attacking creature an opponent controls")
    void cannotTargetOpponentAttackingPirate() {
        Permanent pirate = addCreatureReady(player2, new DireFleetCaptain());
        pirate.setAttacking(true);
        harness.setHand(player1, List.of(new DireFleetPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, pirate.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking Pirate you control");
    }

    private void castPoisoner(UUID targetId) {
        harness.setHand(player1, List.of(new DireFleetPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
