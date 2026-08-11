package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrisonBarricadeTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotPutOnCounterOrAllowAttacking() {
        harness.setHand(player1, List.of(new PrisonBarricade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent barricade = findBarricade();
        assertThat(barricade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        barricade.setSummoningSick(false);

        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void castWithKickerEntersWithCounterAndCanAttackDespiteDefender() {
        harness.setHand(player1, List.of(new PrisonBarricade()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent barricade = findBarricade();
        assertThat(barricade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        barricade.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(barricade.isAttacking()).isTrue();
    }

    private Permanent findBarricade() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PrisonBarricade)
                .findFirst()
                .orElseThrow();
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }
}
