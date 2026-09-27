package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GreatFurnace;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
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

@CardUsed({RoarOfTheKha.class, LeoninSkyhunter.class, GreatFurnace.class})
class RoarOfTheKhaTest extends BaseCardTest {

    @Test
    @DisplayName("Boost mode gives your creatures +1/+1 until end of turn")
    void boostMode() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());

        cast(new int[]{0}, false);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Untap mode untaps all creatures you control")
    void untapMode() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        Permanent ownArtifactLand = harness.addToBattlefieldAndReturn(player1, new GreatFurnace());
        ownCreature.tap();
        opponentCreature.tap();
        ownArtifactLand.tap();

        cast(new int[]{1}, false);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(ownArtifactLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entwine pays the additional cost and resolves both modes")
    void entwined() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        ownCreature.tap();

        cast(new int[]{0, 1}, true);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());

        cast(new int[]{0}, false);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Entwine is rejected without its additional cost")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new RoarOfTheKha()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, boolean entwined) {
        harness.setHand(player1, List.of(new RoarOfTheKha()));
        harness.addMana(player1, ManaColor.WHITE, entwined ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 2 : 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, List.of());
        harness.passBothPriorities();
    }
}
