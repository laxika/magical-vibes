package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
        TavernRuffian.class,
        TavernSmasher.class,
        GrizzlyBears.class
})
class TovolarDireOverlordTest extends BaseCardTest {

    @Test
    void drawsWhenWolfOrWerewolfDealsCombatDamage() {
        addCreatureReady(player1, new TovolarDireOverlord());
        Permanent wolf = addCreatureReady(player1, new SnarlingWolf());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        wolf.setAttacking(true);
        bear.setAttacking(true);

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
        Permanent chosenRuffian = addCreatureReady(player1, new TavernRuffian());
        Permanent unchosenRuffian = addCreatureReady(player1, new TavernRuffian());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        resolveTovolarUpkeepTrigger();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(tovolar.isTransformed()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .validIds()).containsExactlyInAnyOrder(chosenRuffian.getId(), unchosenRuffian.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(chosenRuffian.getId()));

        assertThat(chosenRuffian.isTransformed()).isTrue();
        assertThat(unchosenRuffian.isTransformed()).isFalse();
    }

    @Test
    void mayChooseNoHumanWerewolvesToTransform() {
        gd.dayNight = DayNight.DAY;
        Permanent tovolar = addCreatureReady(player1, new TovolarDireOverlord());
        addCreatureReady(player1, new SnarlingWolf());
        addCreatureReady(player1, new SnarlingWolf());
        Permanent ruffian = addCreatureReady(player1, new TavernRuffian());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        resolveTovolarUpkeepTrigger();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(tovolar.isTransformed()).isTrue();
        assertThat(ruffian.isTransformed()).isFalse();
    }

    @Test
    void doesNotBecomeNightWithFewerThanThreeWolvesOrWerewolves() {
        gd.dayNight = DayNight.DAY;
        addCreatureReady(player1, new TovolarDireOverlord());
        addCreatureReady(player1, new SnarlingWolf());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void backFaceAbilityBoostsControlledWolfAndGrantsTrample() {
        gd.dayNight = DayNight.NIGHT;
        harness.addToBattlefield(player1, new TovolarDireOverlord());
        Permanent tovolar = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        harness.addToBattlefield(player1, new TovolarDireOverlord());
        Permanent tovolar = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(tovolar), 1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void resolveTovolarUpkeepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
