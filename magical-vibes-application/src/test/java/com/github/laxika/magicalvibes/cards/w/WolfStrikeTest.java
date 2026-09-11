package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfStrike.class, GrizzlyBears.class, AirElemental.class})
class WolfStrikeTest extends BaseCardTest {

    @Test
    void dealsBasePowerDamageDuringDay() {
        gd.dayNight = DayNight.DAY;
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(source, target);

        assertThat(source.getPowerModifier()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void boostsSourceAndDealsBoostedPowerDamageAtNight() {
        gd.dayNight = DayNight.NIGHT;
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(source, target);

        assertThat(source.getPowerModifier()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void nightBoostWearsOffAtEndOfTurn() {
        gd.dayNight = DayNight.NIGHT;
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(source, target);
        assertThat(source.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isZero();
    }

    @Test
    void rejectsInvalidTargetControllerChoices() {
        Permanent ownSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingSource = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WolfStrike()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(ownSource.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(opposingSource.getId(), opposingTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent source, Permanent target) {
        harness.setHand(player1, List.of(new WolfStrike()));
        addMana();
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
