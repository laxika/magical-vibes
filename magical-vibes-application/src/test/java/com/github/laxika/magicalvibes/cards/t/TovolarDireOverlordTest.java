package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.m.MercilessPredator;
import com.github.laxika.magicalvibes.cards.r.RecklessWaif;
import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        TovolarDireOverlord.class,
        TovolarTheMidnightScourge.class,
        SnarlingWolf.class,
        RecklessWaif.class,
        MercilessPredator.class,
        CandlegroveWitch.class
})
class TovolarDireOverlordTest extends BaseCardTest {

    @Test
    void drawsWhenWolfOrWerewolfDealsCombatDamage() {
        addCreatureReady(player1, new TovolarDireOverlord());
        Permanent wolf = addCreatureReady(player1, new SnarlingWolf());
        Permanent witch = addCreatureReady(player1, new CandlegroveWitch());
        wolf.setAttacking(true);
        witch.setAttacking(true);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void becomesNightAndTransformsAnyNumberOfChosenHumanWerewolves() {
        gd.dayNight = DayNight.DAY;
        Permanent tovolar = addCreatureReady(player1, new TovolarDireOverlord());
        addCreatureReady(player1, new SnarlingWolf());
        addCreatureReady(player1, new SnarlingWolf());
        Permanent chosenWerewolf = addCreatureReady(player1, new RecklessWaif());
        Permanent unchosenWerewolf = addCreatureReady(player1, new RecklessWaif());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        resolveTovolarUpkeepTrigger();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(tovolar.isTransformed()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .validIds()).containsExactlyInAnyOrder(chosenWerewolf.getId(), unchosenWerewolf.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(chosenWerewolf.getId()));

        assertThat(chosenWerewolf.isTransformed()).isTrue();
        assertThat(unchosenWerewolf.isTransformed()).isFalse();
    }

    @Test
    void mayChooseNoHumanWerewolvesToTransform() {
        gd.dayNight = DayNight.DAY;
        Permanent tovolar = addCreatureReady(player1, new TovolarDireOverlord());
        addCreatureReady(player1, new SnarlingWolf());
        addCreatureReady(player1, new SnarlingWolf());
        Permanent werewolf = addCreatureReady(player1, new RecklessWaif());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        resolveTovolarUpkeepTrigger();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(tovolar.isTransformed()).isTrue();
        assertThat(werewolf.isTransformed()).isFalse();
    }

    @Test
    void doesNotBecomeNightWithFewerThanThreeWolvesOrWerewolves() {
        gd.dayNight = DayNight.DAY;
        addCreatureReady(player1, new TovolarDireOverlord());
        addCreatureReady(player1, new SnarlingWolf());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void backFaceAbilityBoostsControlledWolfAndGrantsTrample() {
        gd.dayNight = DayNight.NIGHT;
        Permanent tovolar = harness.enterBattlefieldAndReturn(player1, new TovolarDireOverlord());
        Permanent wolf = addCreatureReady(player1, new SnarlingWolf());
        int powerBefore = gqs.getEffectivePower(gd, wolf);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tovolar), 2, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(powerBefore + 2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void backFaceAbilityCannotTargetNonWolfCreature() {
        gd.dayNight = DayNight.NIGHT;
        Permanent tovolar = harness.enterBattlefieldAndReturn(player1, new TovolarDireOverlord());
        Permanent witch = addCreatureReady(player1, new CandlegroveWitch());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(tovolar), 1, witch.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsOnceForEachWolfIncludingTovolar() {
        Permanent tovolar = addCreatureReady(player1, new TovolarDireOverlord());
        Permanent firstWolf = addCreatureReady(player1, new SnarlingWolf());
        Permanent secondWolf = addCreatureReady(player1, new SnarlingWolf());
        tovolar.setAttacking(true);
        firstWolf.setAttacking(true);
        secondWolf.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    void backFaceDrawsForItselfAndAnotherWolf() {
        gd.dayNight = DayNight.NIGHT;
        Permanent tovolar = harness.enterBattlefieldAndReturn(player1, new TovolarDireOverlord());
        tovolar.setSummoningSick(false);
        Permanent wolf = addCreatureReady(player1, new SnarlingWolf());
        tovolar.setAttacking(true);
        wolf.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    void doesNotDrawForOpponentsWolf() {
        addCreatureReady(player1, new TovolarDireOverlord());
        Permanent wolf = addCreatureReady(player2, new SnarlingWolf());
        wolf.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void upkeepConditionIsCheckedAgainAtResolution() {
        gd.dayNight = DayNight.DAY;
        Permanent tovolar = addCreatureReady(player1, new TovolarDireOverlord());
        addCreatureReady(player1, new SnarlingWolf());
        Permanent thirdWolf = addCreatureReady(player1, new SnarlingWolf());
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(thirdWolf);
        resolveAllTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(tovolar.isTransformed()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        gd.dayNight = DayNight.DAY;
        Permanent tovolar = addCreatureReady(player1, new TovolarDireOverlord());
        addCreatureReady(player1, new SnarlingWolf());
        addCreatureReady(player1, new SnarlingWolf());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(tovolar.isTransformed()).isFalse();
    }

    @Test
    void transformsMultipleLegacyWerewolvesButNotOpponentsWerewolf() {
        gd.dayNight = DayNight.DAY;
        addCreatureReady(player1, new TovolarDireOverlord());
        Permanent first = addCreatureReady(player1, new RecklessWaif());
        Permanent second = addCreatureReady(player1, new RecklessWaif());
        Permanent opponent = addCreatureReady(player2, new RecklessWaif());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        resolveTovolarUpkeepTrigger();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(first.isTransformed()).isTrue();
        assertThat(second.isTransformed()).isTrue();
        assertThat(opponent.isTransformed()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void backFaceAbilityCannotTargetOpponentsWolf() {
        gd.dayNight = DayNight.NIGHT;
        Permanent tovolar = harness.enterBattlefieldAndReturn(player1, new TovolarDireOverlord());
        Permanent wolf = addCreatureReady(player2, new SnarlingWolf());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(tovolar), 0, wolf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void backFaceAbilityCanTargetItselfWithZeroX() {
        gd.dayNight = DayNight.NIGHT;
        Permanent tovolar = harness.enterBattlefieldAndReturn(player1, new TovolarDireOverlord());
        int powerBefore = gqs.getEffectivePower(gd, tovolar);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tovolar),
                0, tovolar.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tovolar)).isEqualTo(powerBefore);
        assertThat(gqs.hasKeyword(gd, tovolar, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void backFaceBoostAndTrampleExpireAtEndOfTurn() {
        gd.dayNight = DayNight.NIGHT;
        Permanent tovolar = harness.enterBattlefieldAndReturn(player1, new TovolarDireOverlord());
        Permanent wolf = addCreatureReady(player1, new SnarlingWolf());
        int powerBefore = gqs.getEffectivePower(gd, wolf);
        int toughnessBefore = gqs.getEffectiveToughness(gd, wolf);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tovolar),
                2, wolf.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(toughnessBefore);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(toughnessBefore);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    private void resolveTovolarUpkeepTrigger() {
        advanceToUpkeep(player1);
        resolveAllTriggers();
    }
}
