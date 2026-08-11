package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlameBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when no Flame Burst is in any graveyard")
    void dealsTwoDamageWithNoFlameBurstsInGraveyards() {
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts Flame Burst cards in every player's graveyard")
    void countsFlameBurstsInAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new FlameBurst());
        gd.playerGraveyards.get(player2.getId()).add(new FlameBurst());

        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not count other cards in graveyards")
    void ignoresOtherCards() {
        Card other = new LlanowarElves();
        gd.playerGraveyards.get(player1.getId()).add(other);

        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target a creature and kills it with the boosted damage")
    void damagesTargetCreature() {
        gd.playerGraveyards.get(player1.getId()).add(new FlameBurst());
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The resolving copy does not count itself")
    void resolvingCopyDoesNotCountItself() {
        harness.setHand(player1, List.of(new FlameBurst(), new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
