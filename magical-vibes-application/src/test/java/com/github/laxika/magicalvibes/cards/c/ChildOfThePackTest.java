package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.s.SavagePackmate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChildOfThePack.class, SavagePackmate.class, DawnhartDisciple.class})
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(child.isTransformed()).isTrue();
        assertThat(child.getCard()).isInstanceOf(SavagePackmate.class);

        harness.castFromHand(player2, new DawnhartDisciple(), "{1}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player2, new DawnhartDisciple(), "{1}{G}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(child.isTransformed()).isFalse();
        assertThat(child.getCard()).isInstanceOf(ChildOfThePack.class);
    }

    @Test
    void savagePackmateBoostsOtherCreaturesYouControl() {
        gd.dayNight = DayNight.NIGHT;
        Permanent packmate = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());

        assertThat(packmate.getCard()).isInstanceOf(SavagePackmate.class);
        assertThat(gqs.getEffectivePower(gd, packmate)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    void enteringBeforeDayOrNightMakesItDay() {
        gd.dayNight = DayNight.NEITHER;

        Permanent child = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(child.isTransformed()).isFalse();
    }

    @Test
    void oneSpellOnActivePlayersTurnKeepsItDay() {
        gd.dayNight = DayNight.DAY;
        Permanent child = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DawnhartDisciple(), "{1}{G}");
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(child.isTransformed()).isFalse();
    }

    @Test
    void tokenAbilityCanBeActivatedWhileSummoningSickAndTapped() {
        gd.dayNight = DayNight.DAY;
        Permanent child = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());
        child.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(child.isTapped()).isTrue();
    }

    @Test
    void multiplePackmatesBoostEachOtherAndTheirWolves() {
        gd.dayNight = DayNight.DAY;
        Permanent first = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent second = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());
        Permanent wolf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(first.isTransformed()).isTrue();
        assertThat(second.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    void savagePackmateTramplesOverABlocker() {
        gd.dayNight = DayNight.NIGHT;
        Permanent packmate = harness.enterBattlefieldAndReturn(player1, new ChildOfThePack());
        packmate.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
