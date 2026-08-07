package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DisplacementWaveTest extends BaseCardTest {

    private void castForX(int xValue) {
        harness.setHand(player1, List.of(new DisplacementWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        if (xValue > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, xValue);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Bounces nonland permanents with mana value X or less on both sides")
    void bouncesPermanentsWithinX() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        castForX(2);

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Ornithopter", "Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
        harness.assertInGraveyard(player1, "Displacement Wave");
    }

    @Test
    @DisplayName("X=0 only bounces zero-mana-value permanents")
    void xZeroBouncesOnlyFreePermanents() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new LlanowarElves());

        castForX(0);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Ornithopter");
    }

    @Test
    @DisplayName("Lands are never returned regardless of X")
    void landsStay() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new SerraAngel());

        castForX(5);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Island");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Serra Angel");
    }
}
