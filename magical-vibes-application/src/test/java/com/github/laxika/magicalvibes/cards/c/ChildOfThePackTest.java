package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavagePackmate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChildOfThePack.class, SavagePackmate.class, GrizzlyBears.class})
class ChildOfThePackTest extends BaseCardTest {

    @Test
    void activatedAbilityCreatesWolfToken() {
        gd.dayNight = DayNight.DAY;
        Permanent child = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());
        child.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(wolves).hasSize(1);
        Permanent wolf = wolves.getFirst();
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    void dayNightTransformsIntoSavagePackmateAndBack() {
        gd.dayNight = DayNight.DAY;
        Permanent child = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());
        assertThat(child.isTransformed()).isFalse();
        assertThat(child.getCard()).isInstanceOf(ChildOfThePack.class);

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(child.isTransformed()).isTrue();
        assertThat(child.getCard()).isInstanceOf(SavagePackmate.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(child.isTransformed()).isFalse();
        assertThat(child.getCard()).isInstanceOf(ChildOfThePack.class);
    }

    @Test
    void savagePackmateBoostsOtherCreaturesYouControl() {
        gd.dayNight = DayNight.NIGHT;
        Permanent packmate = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(packmate.getCard()).isInstanceOf(SavagePackmate.class);
        assertThat(gqs.getEffectivePower(gd, packmate)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
    }
}
