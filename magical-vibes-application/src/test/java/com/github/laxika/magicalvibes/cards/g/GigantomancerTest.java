package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gigantomancer.class, GrizzlyBears.class})
class GigantomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sets a creature you control's base power and toughness to 7/7")
    void setsTargetBasePowerToughness() {
        addReadyGigantomancer(player1);
        Permanent target = addReadyCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isBasePowerToughnessOverriddenUntilEndOfTurn()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Base power and toughness override wears off at cleanup")
    void wearsOffAtCleanup() {
        addReadyGigantomancer(player1);
        Permanent target = addReadyCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReadyGigantomancer(player1);
        Permanent target = addReadyCreature(player2);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Gigantomancer can target itself repeatedly")
    void canTargetSelfWithoutTappingOrSummoningRestriction() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Gigantomancer());
        source.setSummoningSick(true);
        source.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(7);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Setting base stats preserves the contribution of +1/+1 counters")
    void countersApplyAfterBaseStats() {
        Permanent source = addReadyGigantomancer(player1);
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(9);
        assertThat(source.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability resolves even if Gigantomancer leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent source = addReadyGigantomancer(player1);
        Permanent target = addReadyCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    @DisplayName("A target that changes to an opponent's control is illegal on resolution")
    void rechecksControllerOnResolution() {
        addReadyGigantomancer(player1);
        Permanent target = addReadyCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyGigantomancer(Player player) {
        return addCreatureReady(player, new Gigantomancer());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
