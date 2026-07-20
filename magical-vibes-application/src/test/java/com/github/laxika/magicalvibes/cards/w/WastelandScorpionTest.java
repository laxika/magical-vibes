package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WastelandScorpionTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling {2} discards Wasteland Scorpion and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new WastelandScorpion()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wasteland Scorpion");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
