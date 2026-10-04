package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
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

@CardUsed({DegaDisciple.class, YavimayaCoast.class})
class DegaDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Black ability gives target creature -2/-0 until end of turn")
    void blackAbilityWeakensTargetCreature() {
        Permanent disciple = addReadyDisciple(player1);
        Permanent target = addCreatureReady(player1, new DegaDisciple());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(disciple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Red ability gives target creature +2/+0 until end of turn")
    void redAbilityStrengthensTargetCreature() {
        Permanent disciple = addReadyDisciple(player1);
        Permanent target = addCreatureReady(player1, new DegaDisciple());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(disciple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Abilities can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        addReadyDisciple(player1);
        Permanent opponentCreature = addCreatureReady(player2, new DegaDisciple());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-2);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability modifiers wear off at end of turn")
    void modifiersWearOffAtEndOfTurn() {
        addReadyDisciple(player1);
        Permanent target = addCreatureReady(player1, new DegaDisciple());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Abilities cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent disciple = addReadyDisciple(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(disciple.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void canTargetItself() {
        Permanent disciple = addReadyDisciple(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, disciple.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(1);
    }

    @Test
    void summoningSicknessPreventsBothAbilities() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DegaDisciple());
        disciple.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(disciple.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void tappedDiscipleCannotActivateEitherAbility() {
        Permanent disciple = addReadyDisciple(player1);
        disciple.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void redManaCannotPayForBlackAbility() {
        Permanent disciple = addReadyDisciple(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(disciple.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent disciple = addReadyDisciple(player1);
        Permanent target = addCreatureReady(player2, new DegaDisciple());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(disciple);
        gd.playerGraveyards.get(player1.getId()).add(disciple.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyDisciple(Player player) {
        return addCreatureReady(player, new DegaDisciple());
    }
}
