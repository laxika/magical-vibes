package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpheavalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns every permanent to its owner's hand")
    void returnsEveryPermanentToItsOwnersHand() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card enchantment = new GloriousAnthem();
        Card opponentCreature = new GrizzlyBears();

        harness.addToBattlefield(player1, creature);
        harness.addToBattlefield(player1, land);
        harness.addToBattlefield(player2, enchantment);
        harness.addToBattlefield(player2, opponentCreature);
        harness.setHand(player1, List.of(new Upheaval()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(enchantment, opponentCreature);
    }
}
