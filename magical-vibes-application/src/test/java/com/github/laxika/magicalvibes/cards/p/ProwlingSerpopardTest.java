package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class ProwlingSerpopardTest extends BaseCardTest {

    @Test
    @DisplayName("Prowling Serpopard cannot be countered by Cancel")
    void cannotBeCountered() {
        ProwlingSerpopard serpopard = new ProwlingSerpopard();
        harness.setHand(player1, List.of(serpopard));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, serpopard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prowling Serpopard");
        harness.assertNotInGraveyard(player1, "Prowling Serpopard");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Other creature spells the controller casts cannot be countered")
    void protectsOtherCreatureSpells() {
        harness.addToBattlefield(player1, new ProwlingSerpopard());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }
}
