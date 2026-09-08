package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BogDownTest extends BaseCardTest {

    @Test
    void targetPlayerDiscardsTwoCardsWithoutKicker() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new JalumTome())));
        harness.setHand(player1, List.of(new BogDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedBogDownMakesTargetPlayerDiscardThreeCardsAndSacrificesTwoLands() {
        var firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new JalumTome(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new BogDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castKickedSorceryWithSacrifices(player1, 0, player2.getId(),
                List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickerRequiresExactlyTwoLands() {
        var land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BogDown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrifices(
                player1, 0, player2.getId(), List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice 2");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }
}
