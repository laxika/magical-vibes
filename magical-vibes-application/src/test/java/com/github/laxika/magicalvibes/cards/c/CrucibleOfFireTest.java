package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoardingDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrucibleOfFireTest extends BaseCardTest {

    private Permanent findByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Dragon creatures you control get +3/+3")
    void buffsOwnDragons() {
        harness.addToBattlefield(player1, new HoardingDragon());
        harness.addToBattlefield(player1, new CrucibleOfFire());

        Permanent dragon = findByName(player1, "Hoarding Dragon");

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(7);
    }

    @Test
    @DisplayName("Does not buff non-Dragon creatures")
    void doesNotBuffNonDragons() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrucibleOfFire());

        Permanent bears = findByName(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff Dragons controlled by an opponent")
    void doesNotBuffOpponentDragons() {
        harness.addToBattlefield(player1, new CrucibleOfFire());
        harness.addToBattlefield(player2, new HoardingDragon());

        Permanent opponentDragon = findByName(player2, "Hoarding Dragon");

        assertThat(gqs.getEffectivePower(gd, opponentDragon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentDragon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus is removed when Crucible of Fire leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new HoardingDragon());
        harness.addToBattlefield(player1, new CrucibleOfFire());

        Permanent dragon = findByName(player1, "Hoarding Dragon");
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(7);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Crucible of Fire"));

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(4);
    }
}
