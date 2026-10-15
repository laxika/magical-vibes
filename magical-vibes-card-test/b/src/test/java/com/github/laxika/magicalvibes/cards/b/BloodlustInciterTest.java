package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodlustInciter.class, GrizzlyBears.class, Forest.class})
class BloodlustInciterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability grants haste to target creature")
    void resolvingGrantsHaste() {
        addReadyInciter(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        addReadyInciter(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste is removed at end of turn")
    void hasteRemovedAtEndOfTurn() {
        addReadyInciter(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyInciter(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Tap cost is paid before resolution and prevents a second activation")
    void tapCostPreventsSecondActivation() {
        Permanent inciter = addReadyInciter(player1);

        harness.activateAbility(player1, 0, null, inciter.getId());

        assertThat(inciter.isTapped()).isTrue();
        assertThat(inciter.hasKeyword(Keyword.HASTE)).isFalse();
        harness.passBothPriorities();
        assertThat(inciter.hasKeyword(Keyword.HASTE)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, inciter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Inciter cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent inciter = harness.addToBattlefieldAndReturn(player1, new BloodlustInciter());
        inciter.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, inciter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(inciter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Granted haste lets a newly entered Inciter activate its tap ability")
    void grantedHasteEnablesTapAbility() {
        addReadyInciter(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BloodlustInciter());
        target.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyInciter(Player player) {
        return addCreatureReady(player, new BloodlustInciter());
    }
}