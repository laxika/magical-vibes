package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JewelEyedCobra.class, Shock.class})
class JewelEyedCobraTest extends BaseCardTest {

    @Test
    void createsTreasureWhenItDies() {
        harness.addToBattlefield(player1, new JewelEyedCobra());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Jewel-Eyed Cobra"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jewel-Eyed Cobra");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }
}
