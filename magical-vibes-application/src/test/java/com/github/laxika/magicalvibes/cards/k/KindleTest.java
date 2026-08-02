package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KindleTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when no Kindle is in any graveyard")
    void dealsTwoDamageWithNoKindlesInGraveyards() {
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts Kindle cards in every player's graveyard")
    void countsKindlesInAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new Kindle());
        gd.playerGraveyards.get(player2.getId()).add(new Kindle());

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 + 2 other Kindles = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not count other cards in graveyards")
    void ignoresOtherCards() {
        Card other = new com.github.laxika.magicalvibes.cards.l.LlanowarElves();
        gd.playerGraveyards.get(player1.getId()).add(other);

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target a creature and kills it with the boosted damage")
    void damagesTargetCreature() {
        gd.playerGraveyards.get(player1.getId()).add(new Kindle());
        var bear = harness.addToBattlefieldAndReturn(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        // 2 + 1 = 3 damage kills a 2/2
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The resolving copy does not count itself")
    void resolvingCopyDoesNotCountItself() {
        harness.setHand(player1, List.of(new Kindle(), new Kindle()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // The first Kindle is now in the graveyard, so the second deals 3.
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
