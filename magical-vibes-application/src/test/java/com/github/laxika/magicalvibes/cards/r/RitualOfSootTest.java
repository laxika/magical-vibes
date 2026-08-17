package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class RitualOfSootTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with mana value 3 or less and spares larger creatures and noncreatures")
    void destroysSmallCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrayOgre());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.setHand(player1, List.of(new RitualOfSoot()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Gray Ogre");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Jayemdae Tome");
    }
}
