package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FortifiedAreaTest extends BaseCardTest {

    @Test
    @DisplayName("Wall creatures you control get +1/+0")
    void buffsOwnWalls() {
        harness.addToBattlefield(player1, new FortifiedArea());
        harness.addToBattlefield(player1, new WallOfWood());

        Permanent wall = findPermanent(player1, "Wall of Wood");

        // Wall of Wood is 0/3 → 1/3
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Wall creatures")
    void doesNotBuffNonWalls() {
        harness.addToBattlefield(player1, new FortifiedArea());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Wall creatures")
    void doesNotBuffOpponentWalls() {
        harness.addToBattlefield(player1, new FortifiedArea());
        harness.addToBattlefield(player2, new WallOfWood());

        Permanent opponentWall = findPermanent(player2, "Wall of Wood");

        assertThat(gqs.getEffectivePower(gd, opponentWall)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentWall)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Fortified Area leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new FortifiedArea());
        harness.addToBattlefield(player1, new WallOfWood());

        Permanent wall = findPermanent(player1, "Wall of Wood");
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Fortified Area"));

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }
}
