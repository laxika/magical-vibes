package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrengthOfNight.class, GrizzlyBears.class, MassOfGhouls.class})
class StrengthOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, all your creatures get +1/+1")
    void withoutKickerBoostsAllOwnCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownZombie = harness.addToBattlefieldAndReturn(player1, new MassOfGhouls());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int ownBearPower = gqs.getEffectivePower(gd, ownBear);
        int ownBearToughness = gqs.getEffectiveToughness(gd, ownBear);
        int ownZombiePower = gqs.getEffectivePower(gd, ownZombie);
        int ownZombieToughness = gqs.getEffectiveToughness(gd, ownZombie);
        int opponentBearPower = gqs.getEffectivePower(gd, opponentBear);

        castStrengthOfNight(false);

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(ownBearPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(ownBearToughness + 1);
        assertThat(gqs.getEffectivePower(gd, ownZombie)).isEqualTo(ownZombiePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownZombie)).isEqualTo(ownZombieToughness + 1);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(opponentBearPower);
    }

    @Test
    @DisplayName("With kicker, your Zombies get an additional +2/+2")
    void kickerAddsAdditionalBoostToOwnZombies() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownZombie = harness.addToBattlefieldAndReturn(player1, new MassOfGhouls());
        Permanent opponentZombie = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        int ownBearPower = gqs.getEffectivePower(gd, ownBear);
        int ownZombiePower = gqs.getEffectivePower(gd, ownZombie);
        int ownZombieToughness = gqs.getEffectiveToughness(gd, ownZombie);
        int opponentZombiePower = gqs.getEffectivePower(gd, opponentZombie);

        castStrengthOfNight(true);

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(ownBearPower + 1);
        assertThat(gqs.getEffectivePower(gd, ownZombie)).isEqualTo(ownZombiePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, ownZombie)).isEqualTo(ownZombieToughness + 3);
        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(opponentZombiePower);
    }

    @Test
    @DisplayName("The boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent ownZombie = harness.addToBattlefieldAndReturn(player1, new MassOfGhouls());
        int power = gqs.getEffectivePower(gd, ownZombie);
        int toughness = gqs.getEffectiveToughness(gd, ownZombie);

        castStrengthOfNight(true);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownZombie)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, ownZombie)).isEqualTo(toughness);
    }

    private void castStrengthOfNight(boolean kicked) {
        harness.setHand(player1, java.util.List.of(new StrengthOfNight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        if (kicked) {
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.castKickedInstant(player1, 0);
        } else {
            harness.castInstant(player1, 0);
        }
        harness.passBothPriorities();
    }
}
