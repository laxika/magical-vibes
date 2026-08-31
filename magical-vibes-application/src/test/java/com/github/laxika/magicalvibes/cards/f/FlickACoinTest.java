package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlickACoin.class, GrizzlyBears.class})
class FlickACoinTest extends BaseCardTest {

    @Test
    void dealsDamageToCreatureCreatesTreasureAndDrawsCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlickACoin()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent creature = findPermanent(player2, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void dealsDamageToPlayerCreatesTreasureAndDrawsCard() {
        harness.setHand(player1, List.of(new FlickACoin()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
