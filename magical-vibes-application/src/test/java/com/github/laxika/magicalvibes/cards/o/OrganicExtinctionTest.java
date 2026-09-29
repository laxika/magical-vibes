package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({OrganicExtinction.class, GrizzlyBears.class, HowlingMine.class, Ornithopter.class})
class OrganicExtinctionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonartifact creatures and leaves artifact and noncreature permanents")
    void destroysNonartifactCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HowlingMine());

        castOrganicExtinction();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
    }

    @Test
    @DisplayName("Leaves artifact creatures on the battlefield")
    void leavesArtifactCreatures() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castOrganicExtinction();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void castOrganicExtinction() {
        harness.setHand(player1, List.of(new OrganicExtinction()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
