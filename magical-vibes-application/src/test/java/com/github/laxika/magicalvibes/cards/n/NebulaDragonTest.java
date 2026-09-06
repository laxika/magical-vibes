package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NebulaDragon.class, GrizzlyBears.class})
class NebulaDragonTest extends BaseCardTest {

    @Test
    @DisplayName("When Nebula Dragon enters, it deals 3 damage to a target player")
    void enteringDealsThreeDamageToPlayer() {
        harness.setLife(player2, 20);
        castNebulaDragon(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Nebula Dragon");
    }

    @Test
    @DisplayName("When Nebula Dragon enters, it deals 3 damage to a target creature")
    void enteringDealsThreeDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castNebulaDragon(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castNebulaDragon(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new NebulaDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
