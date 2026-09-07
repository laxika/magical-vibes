package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CullingSun.class, GrizzlyBears.class, HillGiant.class, HowlingMine.class})
class CullingSunTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with mana value 3 or less and spares bigger creatures and noncreatures")
    void destroysMatchingCreaturesOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new HowlingMine());

        harness.setHand(player1, List.of(new CullingSun()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Howling Mine");
        harness.assertInGraveyard(player1, "Culling Sun");
    }
}
