package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurbulentDreams.class, Forest.class, GiantGrowth.class, GrizzlyBears.class})
class TurbulentDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 discards two cards and returns two target nonland permanents")
    void returnsXNonlandPermanentsForTwoDiscardedCards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurbulentDreams(), new GiantGrowth(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<UUID> targetIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getId()).toList();

        harness.castSorceryWithDiscards(player1, 0, 2, targetIds, List.of(1, 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Turbulent Dreams");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Giant Growth") || card.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("X=0 returns no permanents and discards nothing")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurbulentDreams(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TurbulentDreams(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 1, List.of(targetId), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }
}
