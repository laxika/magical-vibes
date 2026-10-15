package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostWarden.class, Forest.class})
class GhostWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability gives a target creature +1/+1 until end of turn")
    void boostsTargetCreature() {
        Permanent source = addCreatureReady(player1, new GhostWarden());
        Permanent target = addCreatureReady(player1, new GhostWarden());
        prepareAbility(source);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(source.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tap ability can target a creature controlled by an opponent")
    void boostsOpponentsCreature() {
        Permanent source = addCreatureReady(player1, new GhostWarden());
        Permanent target = addCreatureReady(player2, new GhostWarden());
        prepareAbility(source);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent source = addCreatureReady(player1, new GhostWarden());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareAbility(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOff() {
        Permanent source = addCreatureReady(player1, new GhostWarden());
        Permanent target = addCreatureReady(player1, new GhostWarden());
        prepareAbility(source);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost a target creature that leaves before resolution")
    void doesNotBoostTargetThatLeavesBeforeResolution() {
        Permanent source = addCreatureReady(player1, new GhostWarden());
        Permanent target = addCreatureReady(player1, new GhostWarden());
        prepareAbility(source);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        Permanent source = addCreatureReady(player1, new GhostWarden());
        prepareAbility(source);

        harness.activateAbility(player1, 0, null, source.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can target itself and the boost waits for resolution")
    void canBoostItself() {
        Permanent source = addCreatureReady(player1, new GhostWarden());
        prepareAbility(source);

        harness.activateAbility(player1, 0, null, source.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability resolves even if its source leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new GhostWarden());
        Permanent target = addCreatureReady(player2, new GhostWarden());
        prepareAbility(source);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts from two Wardens add together")
    void boostsAreCumulative() {
        Permanent first = addCreatureReady(player1, new GhostWarden());
        addCreatureReady(player1, new GhostWarden());
        Permanent target = addCreatureReady(player2, new GhostWarden());
        prepareAbility(first);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }
    private void prepareAbility(Permanent source) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(source.isTapped()).isFalse();
    }
}
