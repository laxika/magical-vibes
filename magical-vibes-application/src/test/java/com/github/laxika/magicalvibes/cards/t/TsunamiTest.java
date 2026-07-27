package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class TsunamiTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all Islands controlled by both players")
    void destroysAllIslands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Tsunami()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Does not destroy non-Island lands")
    void doesNotDestroyNonIslands() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Tsunami()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
    }
}
