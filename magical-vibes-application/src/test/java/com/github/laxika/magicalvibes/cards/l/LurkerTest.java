package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lurker.class, Shock.class, GrizzlyBears.class})
class LurkerTest extends BaseCardTest {

    @Test
    void cannotBeTargetedBeforeAttackingOrBlocking() {
        Permanent lurker = addCreatureReady(player2, new Lurker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, lurker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    void canBeTargetedAfterAttackingThisTurn() {
        Permanent lurker = addCreatureReady(player1, new Lurker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0, lurker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void canBeTargetedAfterBlockingThisTurn() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent lurker = addCreatureReady(player2, new Lurker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.castInstant(player1, 0, lurker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
    }
}
