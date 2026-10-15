package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Creeperhulk.class, ElvishMystic.class})
class CreeperhulkTest extends BaseCardTest {

    @Test
    @DisplayName("Sets a target creature you control to 5/5 and grants trample until end of turn")
    void setsTargetCreatureToFiveFiveAndGrantsTrample() {
        addReadyCreeperhulk();
        Permanent target = addCreatureReady(player1, new ElvishMystic());

        activateCreeperhulk(target);

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The base power, toughness, and trample changes expire at end of turn")
    void changesExpireAtEndOfTurn() {
        addReadyCreeperhulk();
        Permanent target = addCreatureReady(player1, new ElvishMystic());

        activateCreeperhulk(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        addReadyCreeperhulk();
        Permanent target = addCreatureReady(player2, new ElvishMystic());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
    }

    @Test
    @DisplayName("Counters apply on top of the new base power and toughness")
    void countersApplyOnTopOfNewBaseStats() {
        addReadyCreeperhulk();
        Permanent target = addCreatureReady(player1, new ElvishMystic());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        activateCreeperhulk(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Creeperhulk can activate its ability targeting itself")
    void tappedSummoningSickSourceCanTargetItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Creeperhulk());
        source.setSummoningSick(true);
        source.tap();
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activateCreeperhulk(source);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(6);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Repeated activations set the same base stats rather than adding another boost")
    void repeatedActivationsDoNotStackBaseStats() {
        addReadyCreeperhulk();
        Permanent target = addCreatureReady(player1, new ElvishMystic());

        activateCreeperhulk(target);
        activateCreeperhulk(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An ability does not resolve if its target is no longer controlled by its controller")
    void targetChangingControllerBeforeResolutionIsIllegal() {
        addReadyCreeperhulk();
        Permanent target = addCreatureReady(player1, new ElvishMystic());
        addManaForAbility();
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves even if Creeperhulk leaves the battlefield")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addReadyCreeperhulk();
        Permanent target = addCreatureReady(player1, new ElvishMystic());
        addManaForAbility();
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
    private void activateCreeperhulk(Permanent target) {
        addManaForAbility();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyCreeperhulk() {
        return addCreatureReady(player1, new Creeperhulk());
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
