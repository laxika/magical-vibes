package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameBurst.class, DuskImp.class})
class FlameBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when no Flame Burst is in any graveyard")
    void dealsTwoDamageWithNoFlameBurstsInGraveyards() {
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts Flame Burst cards in every player's graveyard")
    void countsFlameBurstsInAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new FlameBurst());
        gd.playerGraveyards.get(player2.getId()).add(new FlameBurst());

        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not count other cards in graveyards")
    void ignoresOtherCards() {
        gd.playerGraveyards.get(player1.getId()).add(new DuskImp());

        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target a creature and kills it with the boosted damage")
    void damagesTargetCreature() {
        gd.playerGraveyards.get(player1.getId()).add(new FlameBurst());
        var imp = harness.addToBattlefieldAndReturn(player2, new DuskImp());

        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, imp.getId());

        harness.assertInGraveyard(player2, "Dusk Imp");
    }

    @Test
    @DisplayName("The resolving copy does not count itself")
    void resolvingCopyDoesNotCountItself() {
        harness.setHand(player1, List.of(new FlameBurst(), new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
