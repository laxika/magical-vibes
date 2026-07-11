package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KirdApeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+2 when controller controls a Forest")
    void boostedWithForest() {
        harness.addToBattlefield(player1, new KirdApe());
        harness.addToBattlefield(player1, new Forest());

        Permanent ape = findPermanent(player1, "Kird Ape");
        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(2); // 1 base + 1
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(3); // 1 base + 2
    }

    @Test
    @DisplayName("No bonus without a Forest")
    void noBonusWithoutForest() {
        harness.addToBattlefield(player1, new KirdApe());

        Permanent ape = findPermanent(player1, "Kird Ape");
        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Forest does not grant bonus")
    void opponentForestDoesNotCount() {
        harness.addToBattlefield(player1, new KirdApe());
        harness.addToBattlefield(player2, new Forest());

        Permanent ape = findPermanent(player1, "Kird Ape");
        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses bonus when the Forest leaves the battlefield")
    void losesBonusWhenForestLeaves() {
        harness.addToBattlefield(player1, new KirdApe());
        harness.addToBattlefield(player1, new Forest());

        Permanent ape = findPermanent(player1, "Kird Ape");
        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.FOREST));

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(1);
    }

}
