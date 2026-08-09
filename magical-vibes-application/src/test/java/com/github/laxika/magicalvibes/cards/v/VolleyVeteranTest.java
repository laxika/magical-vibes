package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GoblinArsonist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolleyVeteranTest extends BaseCardTest {

    @Test
    void dealsOneDamageForItselfAsGoblin() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castVolleyVeteran(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void dealsDamageForEachGoblinControlled() {
        harness.addToBattlefield(player1, new GoblinArsonist());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castVolleyVeteran(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void countsOnlyGoblinsControlledByVolleyVeteransController() {
        harness.addToBattlefield(player2, new GoblinArsonist());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castVolleyVeteran(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new VolleyVeteran()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castVolleyVeteran(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new VolleyVeteran()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
