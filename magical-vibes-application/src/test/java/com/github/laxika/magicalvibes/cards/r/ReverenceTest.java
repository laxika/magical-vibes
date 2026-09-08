package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReverenceTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power 2 or less cannot attack Reverence's controller")
    void preventsSmallCreaturesFromAttackingController() {
        harness.addToBattlefield(player1, new Reverence());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creatures with power 3 or greater can attack Reverence's controller")
    void allowsLargerCreaturesToAttackController() {
        harness.addToBattlefield(player1, new Reverence());
        addCreatureReady(player2, new HillGiant());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
    }
}
