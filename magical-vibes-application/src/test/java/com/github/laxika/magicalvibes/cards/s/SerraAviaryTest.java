package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SerraAviaryTest extends BaseCardTest {

    @Test
    @DisplayName("Own creatures with flying get +1/+1")
    void buffsOwnFliers() {
        harness.addToBattlefield(player1, new SerraAviary());
        harness.addToBattlefield(player1, new AirElemental());

        Permanent elemental = findPermanent(player1, "Air Elemental");

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent creatures with flying also get +1/+1")
    void buffsOpponentFliers() {
        harness.addToBattlefield(player1, new SerraAviary());
        harness.addToBattlefield(player2, new AirElemental());

        Permanent elemental = findPermanent(player2, "Air Elemental");

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creatures without flying are unaffected")
    void doesNotBuffGroundCreatures() {
        harness.addToBattlefield(player1, new SerraAviary());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when Serra Aviary leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new SerraAviary());
        harness.addToBattlefield(player1, new AirElemental());

        Permanent elemental = findPermanent(player1, "Air Elemental");
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Serra Aviary"));

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }
}
