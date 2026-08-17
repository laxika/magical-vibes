package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class CitywideBustTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys every creature with toughness 4 or greater on both battlefields")
    void destroysCreaturesWithToughnessAtLeastFour() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.setHand(player1, List.of(new CitywideBust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Pithing Needle");
    }
}
