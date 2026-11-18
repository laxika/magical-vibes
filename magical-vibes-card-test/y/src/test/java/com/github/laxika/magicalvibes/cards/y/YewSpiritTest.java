package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.j.JointAssault;
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

@CardUsed({YewSpirit.class, JointAssault.class})
class YewSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability once adds X = current power to both stats")
    void activatingOnceDoublesStats() {
        addSpiritReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // Base 3/3, X = 3 → +3/+3 → 6/6.
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(6);
    }

    @Test
    @DisplayName("Activating twice snapshots the boosted power, growing to 12/12")
    void activatingTwiceCompounds() {
        addSpiritReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // After first: 6/6; second X = 6 → +6/+6 → 12/12.
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(12);
    }

    @Test
    @DisplayName("The boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addSpiritReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power is read at resolution and the bonus stays fixed afterward")
    void powerChangesBeforeAndAfterResolution() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new JointAssault(), new JointAssault()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player1, 0, spirit.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(10);

        harness.castInstant(player1, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(12);
    }

    @Test
    @DisplayName("Negative power gives a zero bonus to both stats")
    void negativePowerGivesZeroBonus() {
        Permanent spirit = addSpiritReady(player1);
        PermanentGrants.setPowerBoost(gd, spirit, -4);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability needs neither tapping nor freedom from summoning sickness")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new YewSpirit());
        spirit.setSummoningSick(true);
        spirit.tap();
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(6);
        assertThat(spirit.isTapped()).isTrue();
    }

    private Permanent addSpiritReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new YewSpirit());
        perm.setSummoningSick(false);
        return perm;
    }
}
