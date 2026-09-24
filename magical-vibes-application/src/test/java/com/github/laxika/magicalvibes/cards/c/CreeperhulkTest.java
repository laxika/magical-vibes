package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Creeperhulk.class, GrizzlyBears.class})
class CreeperhulkTest extends BaseCardTest {

    @Test
    @DisplayName("Sets a target creature you control to 5/5 and grants trample until end of turn")
    void setsTargetCreatureToFiveFiveAndGrantsTrample() {
        addReadyCreeperhulk();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        activateCreeperhulk(target);

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The base power, toughness, and trample changes expire at end of turn")
    void changesExpireAtEndOfTurn() {
        addReadyCreeperhulk();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        activateCreeperhulk(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        addReadyCreeperhulk();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
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
