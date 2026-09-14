package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainflailCentipede.class, GrizzlyBears.class})
class ChainflailCentipedeTest extends BaseCardTest {

    @Test
    void attackingUnattachedBoostsItself() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(centipede.getPowerModifier()).isEqualTo(2);
        assertThat(centipede.getToughnessModifier()).isZero();
    }

    @Test
    void attackingEquippedCreatureBoostsTheEquippedCreature() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        centipede.setAttachedTo(creature.getId());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(centipede.getPowerModifier()).isZero();
    }

    @Test
    void attachedCentipedeIsNotACreatureAndReconfigureCanUnattachIt() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, centipede)).isFalse();
        assertThat(centipede.getAttachedTo()).isEqualTo(creature.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(centipede.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, centipede)).isTrue();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(centipede.getAttachedTo()).isNull();
    }

    @Test
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent centipede = addCreatureReady(player1, new ChainflailCentipede());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(centipede.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(centipede.getPowerModifier()).isZero();
    }
}
