package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurbulentDreams.class, AngelOfRetribution.class, TaintedIsle.class})
class TurbulentDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 discards two cards and returns two target nonland permanents")
    void returnsXNonlandPermanentsForTwoDiscardedCards() {
        UUID firstTargetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        UUID secondTargetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        harness.setHand(player1, List.of(new TurbulentDreams(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(firstTargetId, secondTargetId), List.of(1, 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Turbulent Dreams");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Angel of Retribution"))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Angel of Retribution"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Requires exactly X target nonland permanents")
    void requiresExactlyXTargets() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution()).getId();
        harness.setHand(player1, List.of(new TurbulentDreams(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 2, List.of(targetId), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("X=0 returns no permanents and discards nothing")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(new TurbulentDreams(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Angel of Retribution");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new TaintedIsle());
        harness.setHand(player1, List.of(new TurbulentDreams(), new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Tainted Isle");

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 1, List.of(targetId), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }
}
