package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaterkinShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when a flying creature you control enters")
    void flyingAllyEnteringBoosts() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());

        castSuntailHawk(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when a nonflying creature enters")
    void nonFlyingAllyEnteringDoesNotBoost() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());

        castFugitiveWizard(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's flying creature enters")
    void opponentFlyingCreatureEnteringDoesNotBoost() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castSuntailHawk(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying creature boosts stack and wear off at end of turn")
    void flyingAllyBoostsStackAndWearOff() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());

        castSuntailHawk(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        castSuntailHawk(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(1);
    }

    private void castSuntailHawk(Player player) {
        harness.setHand(player, List.of(new SuntailHawk()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.castCreature(player, 0);
    }

    private void castFugitiveWizard(Player player) {
        harness.setHand(player, List.of(new FugitiveWizard()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.castCreature(player, 0);
    }
}
