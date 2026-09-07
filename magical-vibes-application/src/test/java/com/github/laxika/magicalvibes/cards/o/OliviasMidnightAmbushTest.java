package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OliviasMidnightAmbush.class, WorldspineWurm.class, Island.class})
class OliviasMidnightAmbushTest extends BaseCardTest {

    @Test
    void givesTargetCreatureMinusTwoMinusTwoDuringDay() {
        gd.dayNight = DayNight.DAY;
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        cast(target);

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(target.getEffectivePower()).isEqualTo(13);
        assertThat(target.getEffectiveToughness()).isEqualTo(13);
    }

    @Test
    void givesTargetCreatureMinusThirteenMinusThirteenAtNight() {
        gd.dayNight = DayNight.NIGHT;
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        cast(target);

        assertThat(target.getPowerModifier()).isEqualTo(-13);
        assertThat(target.getToughnessModifier()).isEqualTo(-13);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new OliviasMidnightAmbush()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new OliviasMidnightAmbush()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
