package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.m.MistDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolcanicDragon.class, MistDragon.class, IronTuskElephant.class})
class VolcanicDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately after entering the battlefield")
    void canAttackImmediatelyAfterEntering() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player2, new MistDragon());
        harness.setHand(player1, List.of(new VolcanicDragon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Volcanic Dragon");
        declareAttackers(List.of(0));

        assertThat(dragon.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureBlocking() {
        Permanent attacker = addCreatureReady(player1, new VolcanicDragon());
        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isFalse();
    }
}
