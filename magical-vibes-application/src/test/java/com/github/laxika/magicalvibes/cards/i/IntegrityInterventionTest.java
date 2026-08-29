package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntegrityInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Integrity gives a target creature +2/+2 until end of turn")
    void integrityBoostsTargetCreatureUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntegrityIntervention()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Intervention deals 3 damage to a player and gains 3 life")
    void interventionDealsDamageAndGainsLife() {
        harness.setHand(player1, List.of(new IntegrityIntervention()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Integrity cannot target a noncreature permanent")
    void integrityCannotTargetNoncreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new IntegrityIntervention()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
