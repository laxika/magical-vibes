package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoilEruption.class, GrizzlyBears.class})
class RoilEruptionTest extends BaseCardTest {

    @Test
    void dealsThreeDamageWithoutKicker() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void dealsFiveDamageWhenKicked() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castKickedSorceryWithTap(player1, 0, player2.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void canDealDamageToACreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
