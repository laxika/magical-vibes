package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SpatialContortion.class, AirElemental.class, GrizzlyBears.class})
class SpatialContortionTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new SpatialContortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gives target creature +3/-3 until end of turn")
    void appliesBoost() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        castOn(elemental);

        assertThat(elemental.getPowerModifier()).isEqualTo(3);
        assertThat(elemental.getToughnessModifier()).isEqualTo(-3);
        assertThat(elemental.getEffectivePower()).isEqualTo(7);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -3 toughness can kill a small creature")
    void killsSmallCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOn(bears);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The temporary modification wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castOn(elemental);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(0);
        assertThat(elemental.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SpatialContortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
