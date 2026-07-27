package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class VirtuesRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys white creatures controlled by both players")
    void destroysWhiteCreatures() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new VirtuesRuin()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        harness.assertInGraveyard(player1, "Elite Vanguard");
        harness.assertInGraveyard(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Leaves non-white creatures untouched")
    void leavesNonWhiteCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new VirtuesRuin()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Destroys only white creatures among a mixed board")
    void destroysOnlyWhiteAmongMixed() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new VirtuesRuin()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }
}
