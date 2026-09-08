package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SinisterCryologist.class, GrizzlyBears.class})
class SinisterCryologistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an opponent's creature -3/-0 until end of turn")
    void etbDebuffsOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB ability cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SinisterCryologist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("The -3/-0 effect wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Warp casts for {U}, applies its ETB ability, and exiles at the next end step")
    void warpCastsForAlternateCostAndExilesAtNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SinisterCryologist cryologist = new SinisterCryologist();
        harness.setHand(player1, List.of(cryologist));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(cryologist.getId())).isNotNull();
    }

    private void castAndResolve(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SinisterCryologist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
