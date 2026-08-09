package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShriekingMoggTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all other creatures on both battlefields")
    void etbTapsAllOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShriekingMogg()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not tap Shrieking Mogg itself")
    void etbDoesNotTapSelf() {
        harness.setHand(player1, List.of(new ShriekingMogg()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Shrieking Mogg").isTapped()).isFalse();
    }
}
