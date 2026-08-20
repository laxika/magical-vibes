package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WoodElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkemfarShadowsageTest extends BaseCardTest {

    private void castShadowsage() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new WoodElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkemfarShadowsage()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void eachOpponentLosesTheLargestSharedCreatureTypeCount() {
        int opponentLifeBefore = gd.getLife(player2.getId());

        castShadowsage();
        harness.handleListChoice(player1, "Each opponent loses X life.");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    void controllerGainsTheLargestSharedCreatureTypeCount() {
        int controllerLifeBefore = gd.getLife(player1.getId());

        castShadowsage();
        harness.handleListChoice(player1, "You gain X life.");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 3);
    }
}
