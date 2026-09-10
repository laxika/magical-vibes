package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViciousRivalryTest extends BaseCardTest {

    private void castForX(int xValue) {
        harness.setHand(player1, List.of(new ViciousRivalry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2 + xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Pays X life and destroys artifacts and creatures with mana value X or less")
    void destroysArtifactsAndCreaturesWithinX() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Island());

        castForX(2);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("X=0 destroys only zero-mana-value artifacts and creatures")
    void xZeroDestroysOnlyFreePermanents() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castForX(0);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot pay more life than the caster has")
    void cannotPayMoreLifeThanAvailable() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new ViciousRivalry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 4))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 3);
    }
}
