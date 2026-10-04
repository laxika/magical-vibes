package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RhythmOfTheWild;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenziedArynx.class, RhythmOfTheWild.class})
class FrenziedArynxTest extends BaseCardTest {

    @Test
    @DisplayName("Riot can give Frenzied Arynx a +1/+1 counter")
    void riotAddsCounter() {
        Permanent arynx = castArynx(true);

        assertThat(arynx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, arynx)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, arynx)).isEqualTo(4);
    }

    @Test
    @DisplayName("Riot can give Frenzied Arynx haste")
    void riotAddsHaste() {
        Permanent arynx = castArynx(false);

        assertThat(gqs.hasKeyword(gd, arynx, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The activated ability gives Frenzied Arynx +3/+0 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent arynx = addReadyArynx();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, arynx)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, arynx)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, arynx)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, arynx)).isEqualTo(3);
    }

    @Test
    @DisplayName("Rhythm of the Wild lets both instances of riot add a counter")
    void multipleRiotInstancesAddSeparateCounters() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());

        Permanent arynx = castArynx(true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(arynx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, arynx)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, arynx)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, arynx, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Rhythm of the Wild allows choosing a counter and haste independently")
    void multipleRiotInstancesAllowCounterAndHaste() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());

        Permanent arynx = castArynx(true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(arynx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, arynx, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste chosen for riot remains after end of turn")
    void riotHastePersistsAfterCleanup() {
        Permanent arynx = castArynx(false);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, arynx, Keyword.HASTE)).isTrue();
        assertThat(arynx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability can be used while summoning sick and stacks with itself")
    void activatedAbilityStacksWhileSummoningSick() {
        Permanent arynx = castArynx(true);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.getEffectivePower(gd, arynx)).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, arynx)).isEqualTo(7);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, arynx)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, arynx)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, arynx)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, arynx)).isEqualTo(4);
        assertThat(arynx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castArynx(boolean chooseCounter) {
        harness.setHand(player1, List.of(new FrenziedArynx()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, chooseCounter);
        }
        return gqs.findPermanentById(gd, harness.getPermanentId(player1, "Frenzied Arynx"));
    }

    private Permanent addReadyArynx() {
        Permanent arynx = harness.addToBattlefieldAndReturn(player1, new FrenziedArynx());
        arynx.setSummoningSick(false);
        return arynx;
    }
}
