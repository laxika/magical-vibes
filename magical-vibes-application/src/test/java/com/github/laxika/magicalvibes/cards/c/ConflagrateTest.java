package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Conflagrate.class, GiantGrowth.class, GrizzlyBears.class})
class ConflagrateTest extends BaseCardTest {

    @Test
    void normalCastDividesXDamageAmongAnyTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Conflagrate()));
        harness.addMana(player1, ManaColor.RED, 7);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorceryForX(player1, 0, 3, Map.of(bears.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void flashbackRequiresAndPaysDiscardXCards() {
        harness.setGraveyard(player1, List.of(new Conflagrate()));
        harness.setHand(player1, new ArrayList<>(List.of(
                new GiantGrowth(), new GrizzlyBears(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFlashbackForXWithDiscards(player1, 0, 3, Map.of(player2.getId(), 3), List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Giant Growth", "Giant Growth", "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Conflagrate"));
    }

    @Test
    void flashbackIsRejectedWhenTheHandCannotCoverXDiscards() {
        harness.setGraveyard(player1, List.of(new Conflagrate()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashbackForXWithDiscards(
                player1, 0, 2, Map.of(player2.getId(), 2), List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }
}
