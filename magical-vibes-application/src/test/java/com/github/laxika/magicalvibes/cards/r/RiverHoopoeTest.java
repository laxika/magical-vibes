package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiverHoopoeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3}{G}{U} gains 2 life and draws a card")
    void activateGainsLifeAndDraws() {
        Permanent hoopoe = harness.addToBattlefieldAndReturn(player1, new RiverHoopoe());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hoopoe);

        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent hoopoe = harness.addToBattlefieldAndReturn(player1, new RiverHoopoe());
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hoopoe);

        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
