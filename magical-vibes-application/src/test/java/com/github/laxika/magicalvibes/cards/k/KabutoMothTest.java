package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KabutoMoth.class, IsamaruHoundOfKonda.class, Plains.class})
class KabutoMothTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature +1/+2 until end of turn")
    void boostsTargetCreature() {
        Permanent target = addKabutoMothAndTarget();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Taps Kabuto Moth when its ability is activated")
    void tapsOnActivation() {
        Permanent target = addKabutoMothAndTarget();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(findPermanent(player1, "Kabuto Moth").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the tap ability while Kabuto Moth has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent moth = harness.addToBattlefieldAndReturn(player1, new KabutoMoth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, moth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate the tap ability while Kabuto Moth is already tapped")
    void cannotActivateWhenTapped() {
        Permanent target = addKabutoMothAndTarget();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can boost a creature controlled by an opponent")
    void boostsOpponentsCreature() {
        addReadyKabutoMoth();
        Permanent target = addCreatureReady(player2, new IsamaruHoundOfKonda());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyKabutoMoth();
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(findPermanent(player1, "Kabuto Moth").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addReadyKabutoMoth();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Kabuto Moth").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not boost a target creature that leaves before resolution")
    void doesNotBoostTargetThatLeavesBeforeResolution() {
        Permanent target = addKabutoMothAndTarget();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent target = addKabutoMothAndTarget();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private Permanent addKabutoMothAndTarget() {
        addReadyKabutoMoth();
        return addCreatureReady(player1, new IsamaruHoundOfKonda());
    }

    private void addReadyKabutoMoth() {
        addCreatureReady(player1, new KabutoMoth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
