package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CeremoniousRejectionTest extends BaseCardTest {

    @Test
    void countersColorlessSpell() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player2, List.of(ornithopter));
        harness.setHand(player1, List.of(new CeremoniousRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, ornithopter.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }

    @Test
    void cannotTargetColoredSpell() {
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        harness.setHand(player2, List.of(grizzlyBears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new CeremoniousRejection()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, grizzlyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
