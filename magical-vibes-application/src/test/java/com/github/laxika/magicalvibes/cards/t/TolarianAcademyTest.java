package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TolarianAcademyTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one blue mana for each artifact its controller controls")
    void addsBlueManaForControlledArtifacts() {
        Permanent academy = harness.addToBattlefieldAndReturn(player1, new TolarianAcademy());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(academy.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no mana when its controller controls no artifacts")
    void addsNoManaWithoutArtifacts() {
        harness.addToBattlefield(player1, new TolarianAcademy());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
}
