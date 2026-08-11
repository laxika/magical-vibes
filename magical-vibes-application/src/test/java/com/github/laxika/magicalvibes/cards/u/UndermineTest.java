package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UndermineTest extends BaseCardTest {

    @Test
    void countersTargetSpellAndItsControllerLosesThreeLife() {
        LlanowarElves elves = new LlanowarElves();
        Undermine undermine = new Undermine();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(undermine));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elves);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(undermine);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 17);
    }

    @Test
    void doesNotLoseLifeWhenTargetSpellIsNoLongerOnTheStack() {
        LlanowarElves elves = new LlanowarElves();
        Undermine undermine = new Undermine();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(undermine));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(undermine);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
