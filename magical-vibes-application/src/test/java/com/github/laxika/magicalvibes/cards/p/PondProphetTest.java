package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PondProphet.class)
class PondProphetTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new PondProphet()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new PondProphet()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertOnBattlefield(player1, "Pond Prophet");
    }
}
