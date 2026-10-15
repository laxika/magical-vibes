package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CandlelitCavalry;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
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

@CardUsed({OliviasMidnightAmbush.class, WorldspineWurm.class, Island.class, CandlelitCavalry.class})
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

    @Test
    void givesMinusTwoMinusTwoWhenNeitherDayNorNightAndCanTargetOwnCreature() {
        gd.dayNight = DayNight.NEITHER;
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());

        cast(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.dayNight).isEqualTo(DayNight.NEITHER);
    }

    @Test
    void usesNightDesignationAtResolutionInsteadOfAtCasting() {
        gd.dayNight = DayNight.DAY;
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlelitCavalry());
        harness.setHand(player1, List.of(new OliviasMidnightAmbush()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        gd.dayNight = DayNight.NIGHT;
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Candlelit Cavalry");
    }

    @Test
    void usesDayDesignationIfNightEndsBeforeResolution() {
        gd.dayNight = DayNight.NIGHT;
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlelitCavalry());
        harness.setHand(player1, List.of(new OliviasMidnightAmbush()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        gd.dayNight = DayNight.DAY;
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void reductionRemainsFixedWhenNightBeginsAfterResolutionAndExpiresAtEndOfTurn() {
        gd.dayNight = DayNight.DAY;
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlelitCavalry());

        cast(target);
        gd.dayNight = DayNight.NIGHT;

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new OliviasMidnightAmbush()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
