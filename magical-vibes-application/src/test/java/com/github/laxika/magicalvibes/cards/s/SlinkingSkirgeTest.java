package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlinkingSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices as an activation cost and draws a card")
    void sacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new SlinkingSkirge());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Slinking Skirge");
        harness.assertInGraveyard(player1, "Slinking Skirge");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

}
