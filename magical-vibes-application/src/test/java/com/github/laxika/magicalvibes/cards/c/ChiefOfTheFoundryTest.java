package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChiefOfTheFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("Other artifact creatures you control get +1/+1")
    void buffsOtherArtifactCreatures() {
        harness.addToBattlefield(player1, new ChiefOfTheFoundry());
        harness.addToBattlefield(player1, new Ornithopter());

        Permanent ornithopter = findPermanent(player1, "Ornithopter");

        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Chief of the Foundry does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ChiefOfTheFoundry());

        Permanent chief = findPermanent(player1, "Chief of the Foundry");

        assertThat(gqs.getEffectivePower(gd, chief)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chief)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff nonartifact creatures")
    void doesNotBuffNonArtifactCreatures() {
        harness.addToBattlefield(player1, new ChiefOfTheFoundry());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff artifact creatures an opponent controls")
    void doesNotBuffOpponentArtifactCreatures() {
        harness.addToBattlefield(player1, new ChiefOfTheFoundry());
        harness.addToBattlefield(player2, new Ornithopter());

        Permanent opponentThopter = findPermanent(player2, "Ornithopter");

        assertThat(gqs.getEffectivePower(gd, opponentThopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentThopter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Chiefs buff each other")
    void twoChiefsBuffEachOther() {
        harness.addToBattlefield(player1, new ChiefOfTheFoundry());
        harness.addToBattlefield(player1, new ChiefOfTheFoundry());

        List<Permanent> chiefs = findPermanents(player1, "Chief of the Foundry");

        assertThat(chiefs).hasSize(2);
        for (Permanent chief : chiefs) {
            assertThat(gqs.getEffectivePower(gd, chief)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, chief)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Bonus is removed when Chief of the Foundry leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new ChiefOfTheFoundry());
        harness.addToBattlefield(player1, new Ornithopter());

        Permanent ornithopter = findPermanent(player1, "Ornithopter");

        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Chief of the Foundry"));

        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(2);
    }
}
