package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EiganjoFreeRiders;
import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reverence.class, HandOfHonor.class, EiganjoFreeRiders.class})
class ReverenceTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power 2 or less cannot attack Reverence's controller")
    void preventsSmallCreaturesFromAttackingController() {
        harness.addToBattlefield(player1, new Reverence());
        addCreatureReady(player2, new HandOfHonor());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creatures with power 3 or greater can attack Reverence's controller")
    void allowsLargerCreaturesToAttackController() {
        harness.addToBattlefield(player1, new Reverence());
        addCreatureReady(player2, new EiganjoFreeRiders());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
    }
}
