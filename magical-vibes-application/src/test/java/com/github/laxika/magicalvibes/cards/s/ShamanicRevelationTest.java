package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShamanicRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Draws for each controlled creature and gains 4 life for each creature with power 4 or greater")
    void drawsAndGainsLifeForControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ShamanicRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Does not gain ferocious life when no controlled creature has power 4 or greater")
    void doesNotGainFerociousLifeWithoutHighPowerCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ShamanicRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
    }
}
