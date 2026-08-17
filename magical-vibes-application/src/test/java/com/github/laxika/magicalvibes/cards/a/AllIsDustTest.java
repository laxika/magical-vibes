package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class AllIsDustTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices every colored permanent but keeps colorless permanents")
    void sacrificesColoredPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Island");
        harness.assertInGraveyard(player2, "Mountain");
    }
}
