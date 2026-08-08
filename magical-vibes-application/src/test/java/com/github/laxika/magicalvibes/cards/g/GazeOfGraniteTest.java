package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class GazeOfGraniteTest extends BaseCardTest {

    private void castGaze(int xValue) {
        harness.setHand(player1, List.of(new GazeOfGranite()));
        harness.addMana(player1, ManaColor.BLACK, 2 + xValue);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys nonland permanents with mana value X or less and spares bigger ones")
    void destroysPermanentsWithinManaValueBound() {
        harness.addToBattlefield(player1, new LlanowarElves()); // mana value 1
        harness.addToBattlefield(player2, new HillGiant()); // mana value 4
        harness.addToBattlefield(player2, new GrizzlyBears()); // mana value 2

        castGaze(2);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Spares lands regardless of their mana value")
    void sparesLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castGaze(3);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys noncreature permanents such as artifacts")
    void destroysNoncreaturePermanents() {
        harness.addToBattlefield(player2, new SerraAngel()); // mana value 5, survives
        harness.addToBattlefield(player2, new HowlingMine()); // mana value 2

        castGaze(2);

        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("X=0 destroys only mana value 0 permanents")
    void xZeroDestroysOnlyZeroCostPermanents() {
        harness.addToBattlefield(player2, new LlanowarElves()); // mana value 1

        castGaze(0);

        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }
}
