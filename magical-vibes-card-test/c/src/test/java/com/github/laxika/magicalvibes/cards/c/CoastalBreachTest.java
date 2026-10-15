package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoastalBreach.class, AngelsFeather.class, GloriousAnthem.class, GrizzlyBears.class,
        Island.class})
class CoastalBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all nonland permanents on all battlefields to their owners' hands")
    void returnsAllNonlandPermanentsToTheirOwnersHands() {
        Card ownArtifact = new AngelsFeather();
        Card ownCreature = new GrizzlyBears();
        Card ownLand = new Island();
        Card opposingEnchantment = new GloriousAnthem();
        Card opposingCreature = new GrizzlyBears();
        Card opposingLand = new Island();

        harness.addToBattlefield(player1, ownArtifact);
        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player1, ownLand);
        harness.addToBattlefield(player2, opposingEnchantment);
        harness.addToBattlefield(player2, opposingCreature);
        harness.addToBattlefield(player2, opposingLand);
        harness.setHand(player1, List.of(new CoastalBreach()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard()).isSameAs(ownLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getCard()).isSameAs(opposingLand);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(ownArtifact, ownCreature);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(opposingEnchantment, opposingCreature);
    }
}
