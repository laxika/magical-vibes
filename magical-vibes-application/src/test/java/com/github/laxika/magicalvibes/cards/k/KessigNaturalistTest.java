package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LordOfTheUlvenwald;
import com.github.laxika.magicalvibes.cards.p.PlayWithFire;
import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KessigNaturalist.class, LordOfTheUlvenwald.class, PlayWithFire.class,
        SnarlingWolf.class, TimberlandGuide.class})
class KessigNaturalistTest extends BaseCardTest {

    @Test
    void frontFaceAttackAddsChosenPersistentMana() {
        addCreatureReady(player1, new KessigNaturalist());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Add {R}");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getPersistentMana(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    void backFaceAttackAddsChosenPersistentMana() {
        addCreatureReady(player1, new LordOfTheUlvenwald());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Add {G}");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.getPersistentMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    void backFaceBoostsOnlyOwnOtherWolvesAndWerewolves() {
        Permanent lord = addCreatureReady(player1, new LordOfTheUlvenwald());
        Permanent ownWolf = addCreatureReady(player1, new SnarlingWolf());
        Permanent opposingWolf = addCreatureReady(player2, new SnarlingWolf());

        assertThat(gqs.getEffectivePower(gd, ownWolf)).isEqualTo(gqs.getEffectivePower(gd, opposingWolf) + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownWolf)).isEqualTo(gqs.getEffectiveToughness(gd, opposingWolf) + 1);
        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(lord.getBasePower());
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(lord.getBaseToughness());
    }

    @Test
    void dayAndNightTransformTheFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent naturalist = harness.addToBattlefieldAndReturn(player1, new KessigNaturalist());

        gd.spellsCastLastTurn.clear();
        advanceToNextTurn(player1);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(naturalist.isTransformed()).isTrue();
        assertThat(naturalist.getCard()).isInstanceOf(LordOfTheUlvenwald.class);

        gd.recordSpellCast(player2.getId(), new KessigNaturalist());
        gd.recordSpellCast(player2.getId(), new KessigNaturalist());
        advanceToNextTurn(player2);
        assertThat(naturalist.isTransformed()).isFalse();
        assertThat(naturalist.getCard()).isInstanceOf(KessigNaturalist.class);
    }

    @Test
    void attackManaSurvivesCombatAndEndStepButExpiresAtTurnEnd() {
        addCreatureReady(player1, new KessigNaturalist());
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.handleListChoice(player1, "Add {G}"));

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getPersistentMana(ManaColor.GREEN)).isZero();
    }

    @Test
    void backFaceAttackStillAddsManaAfterSourceLeavesBattlefield() {
        Permanent lord = addCreatureReady(player1, new LordOfTheUlvenwald());
        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lord));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Add {R}");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void backFaceBoostsOtherWerewolvesOnceAndDoesNotBoostHumans() {
        Permanent naturalist = addCreatureReady(player1, new KessigNaturalist());
        Permanent human = addCreatureReady(player1, new TimberlandGuide());
        int naturalistPower = gqs.getEffectivePower(gd, naturalist);
        int naturalistToughness = gqs.getEffectiveToughness(gd, naturalist);
        int humanPower = gqs.getEffectivePower(gd, human);
        int humanToughness = gqs.getEffectiveToughness(gd, human);

        addCreatureReady(player1, new LordOfTheUlvenwald());

        assertThat(gqs.getEffectivePower(gd, naturalist)).isEqualTo(naturalistPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, naturalist)).isEqualTo(naturalistToughness + 1);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(humanPower);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(humanToughness);
    }

    @Test
    void multipleLordsBoostEachOtherAndTheirBonusesStack() {
        Permanent wolf = addCreatureReady(player1, new SnarlingWolf());
        int wolfPower = gqs.getEffectivePower(gd, wolf);
        int wolfToughness = gqs.getEffectiveToughness(gd, wolf);
        Permanent firstLord = addCreatureReady(player1, new LordOfTheUlvenwald());
        Permanent secondLord = addCreatureReady(player1, new LordOfTheUlvenwald());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(wolfPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(wolfToughness + 2);
        assertThat(gqs.getEffectivePower(gd, firstLord)).isEqualTo(firstLord.getBasePower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, firstLord)).isEqualTo(firstLord.getBaseToughness() + 1);
        assertThat(gqs.getEffectivePower(gd, secondLord)).isEqualTo(secondLord.getBasePower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, secondLord)).isEqualTo(secondLord.getBaseToughness() + 1);
    }

    @Test
    void enteringWhenNeitherDayNorNightMakesItDay() {
        gd.dayNight = DayNight.NEITHER;
        Permanent naturalist = harness.enterBattlefieldAndReturn(player1, new KessigNaturalist());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(naturalist.isTransformed()).isFalse();
    }

    @Test
    void enteringAtNightUsesBackFaceAndImmediatelyBoostsWolves() {
        gd.dayNight = DayNight.NIGHT;
        Permanent wolf = addCreatureReady(player1, new SnarlingWolf());
        int wolfPower = gqs.getEffectivePower(gd, wolf);
        int wolfToughness = gqs.getEffectiveToughness(gd, wolf);

        Permanent naturalist = harness.enterBattlefieldAndReturn(player1, new KessigNaturalist());

        assertThat(naturalist.isTransformed()).isTrue();
        assertThat(naturalist.getCard()).isInstanceOf(LordOfTheUlvenwald.class);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(wolfPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(wolfToughness + 1);
    }

    @Test
    void opponentsSpellsDoNotPreventNightOrCauseDay() {
        gd.dayNight = DayNight.DAY;
        Permanent naturalist = harness.addToBattlefieldAndReturn(player1, new KessigNaturalist());
        gd.recordSpellCast(player2.getId(), new PlayWithFire());
        gd.recordSpellCast(player2.getId(), new PlayWithFire());

        advanceToNextTurn(player1);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(naturalist.isTransformed()).isTrue();

        gd.recordSpellCast(player1.getId(), new PlayWithFire());
        gd.recordSpellCast(player1.getId(), new PlayWithFire());
        advanceToNextTurn(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(naturalist.isTransformed()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = DayNight.class, names = {"DAY", "NIGHT"})
    void oneSpellDuringActivePlayersTurnDoesNotChangeDayOrNight(DayNight designation) {
        harness.forceActivePlayer(player1);
        gd.dayNight = designation;
        Permanent naturalist = harness.enterBattlefieldAndReturn(player1, new KessigNaturalist());
        gd.recordSpellCast(player1.getId(), new KessigNaturalist());

        advanceToNextTurn(player1);

        assertThat(gd.dayNight).isEqualTo(designation);
        assertThat(naturalist.isTransformed()).isEqualTo(designation == DayNight.NIGHT);
    }

    @Test
    void transformingToFrontFaceRemovesTheWolfBonus() {
        gd.dayNight = DayNight.DAY;
        harness.enterBattlefieldAndReturn(player1, new KessigNaturalist());
        Permanent wolf = addCreatureReady(player1, new SnarlingWolf());
        int wolfPower = gqs.getEffectivePower(gd, wolf);
        int wolfToughness = gqs.getEffectiveToughness(gd, wolf);

        advanceToNextTurn(player1);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(wolfPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(wolfToughness + 1);

        gd.recordSpellCast(player2.getId(), new KessigNaturalist());
        gd.recordSpellCast(player2.getId(), new KessigNaturalist());
        advanceToNextTurn(player2);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(wolfPower);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(wolfToughness);
    }

    private void advanceToNextTurn(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(activePlayer.equals(player1) ? player2 : player1, TurnStep.UPKEEP);
    }
}
