package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoartuskLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Other red creatures you control get +1/+1")
    void buffsOwnRedCreatures() {
        harness.addToBattlefield(player1, new BoartuskLiege());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, hillGiant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hillGiant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Other green creatures you control get +1/+1")
    void buffsOwnGreenCreatures() {
        harness.addToBattlefield(player1, new BoartuskLiege());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures that are neither red nor green are not buffed")
    void doesNotBuffOtherColors() {
        harness.addToBattlefield(player1, new BoartuskLiege());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's red creatures are not buffed")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new BoartuskLiege());
        Permanent opponentGiant = addCreatureReady(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, opponentGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentGiant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new BoartuskLiege());

        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(4);
    }

    @Test
    @DisplayName("A red-green creature gets both bonuses (+2/+2)")
    void redGreenCreatureGetsBothBonuses() {
        harness.addToBattlefield(player1, new BoartuskLiege());
        // A second Liege is both red and green, so it gets +1/+1 twice from the first.
        Permanent secondLiege = addCreatureReady(player1, new BoartuskLiege());

        assertThat(gqs.getEffectivePower(gd, secondLiege)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondLiege)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bonus is removed when the Liege leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new BoartuskLiege());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Boartusk Liege"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
