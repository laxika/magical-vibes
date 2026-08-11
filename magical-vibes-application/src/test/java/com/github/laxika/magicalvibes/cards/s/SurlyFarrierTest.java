package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyFarrier(Player player) {
        Permanent farrier = new Permanent(new SurlyFarrier());
        farrier.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(farrier);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return farrier;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
