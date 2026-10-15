package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.InventorsGoggles;
import com.github.laxika.magicalvibes.cards.t.ThrivingRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrnamentalCourage.class, ThrivingRhino.class, InventorsGoggles.class})
class OrnamentalCourageTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and boosts the target creature")
    void untapsAndBoostsTarget() {
        Permanent target = addTappedCreature(player2);

        castOrnamentalCourage(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = addTappedCreature(player1);
        castOrnamentalCourage(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new InventorsGoggles());
        harness.setHand(player1, List.of(new OrnamentalCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An already untapped creature still gets the boost")
    void boostsUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThrivingRhino());

        castOrnamentalCourage(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Only the targeted creature is untapped and boosted")
    void affectsOnlyTarget() {
        Permanent target = addTappedCreature(player1);
        Permanent other = addTappedCreature(player1);

        castOrnamentalCourage(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The spell has no effect when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = addTappedCreature(player2);
        Permanent other = addTappedCreature(player2);
        harness.setHand(player1, List.of(new OrnamentalCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof OrnamentalCourage);
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    private void castOrnamentalCourage(Permanent target) {
        harness.setHand(player1, List.of(new OrnamentalCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new ThrivingRhino());
        creature.setSummoningSick(false);
        creature.tap();
        return creature;
    }
}
