package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StripedRiverwinderTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling {U} discards Striped Riverwinder and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new StripedRiverwinder()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Striped Riverwinder");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
