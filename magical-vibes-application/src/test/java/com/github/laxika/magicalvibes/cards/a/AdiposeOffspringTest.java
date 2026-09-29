package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdiposeOffspring.class, GrizzlyBears.class})
class AdiposeOffspringTest extends BaseCardTest {

    @Test
    void hardcastCreatesOneAlien() {
        harness.setHand(player1, List.of(new AdiposeOffspring()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien")).hasSize(1);
    }

    @Test
    void emergeCreatesAliensEqualToSacrificedToughness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var sacrificedId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new AdiposeOffspring()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien")).hasSize(2);
    }
}
