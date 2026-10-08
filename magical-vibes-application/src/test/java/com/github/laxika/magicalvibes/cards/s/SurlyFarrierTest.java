package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SurlyFarrier.class})
class SurlyFarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives a creature you control +1/+1 and vigilance")
    void abilityBoostsAndGrantsVigilance() {
        addReadyFarrier(player1);
        Permanent target = addCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReadyFarrier(player1);
        Permanent target = addCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void onlyAtSorcerySpeed() {
        addReadyFarrier(player1);
        Permanent target = addCreature(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addReadyFarrier(player1);
        Permanent target = addCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void canTargetItselfAndPaysTapCostBeforeResolution() {
        Permanent farrier = addReadyFarrier(player1);

        harness.activateAbility(player1, 0, null, farrier.getId());

        assertThat(farrier.isTapped()).isTrue();
        assertThat(farrier.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, farrier, Keyword.VIGILANCE)).isFalse();

        harness.passBothPriorities();

        assertThat(farrier.getPowerModifier()).isEqualTo(1);
        assertThat(farrier.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, farrier, Keyword.VIGILANCE)).isTrue();
        assertThat(farrier.isTapped()).isTrue();
    }

    @Test
    void summoningSickFarrierCannotActivate() {
        Permanent farrier = addReadyFarrier(player1);
        farrier.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, farrier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(farrier.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedFarrierCannotActivate() {
        Permanent farrier = addReadyFarrier(player1);
        farrier.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, farrier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhaseOnOwnTurn() {
        Permanent farrier = addReadyFarrier(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, farrier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(farrier.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithAnotherAbilityOnStack() {
        Permanent first = addReadyFarrier(player1);
        Permanent second = addCreature(player1);
        harness.activateAbility(player1, 0, null, first.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, second.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(second.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void canActivateInPostcombatMainPhase() {
        Permanent farrier = addReadyFarrier(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, farrier.getId());
        harness.passBothPriorities();

        assertThat(farrier.getPowerModifier()).isEqualTo(1);
        assertThat(farrier.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, farrier, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent farrier = addReadyFarrier(player1);
        Permanent target = addCreature(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(farrier);
        gd.playerGraveyards.get(player1.getId()).add(farrier.getCard());

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void targetBecomesIllegalWhenOpponentGainsControlBeforeResolution() {
        addReadyFarrier(player1);
        Permanent target = addCreature(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyFarrier(Player player) {
        Permanent farrier = addCreatureReady(player, new SurlyFarrier());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return farrier;
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new SurlyFarrier());
    }
}
