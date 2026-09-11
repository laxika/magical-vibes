package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShepherdOfHeroes.class, BoggartBrute.class, FaerieMiscreant.class, FugitiveWizard.class})
class ShepherdOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each creature in your party, including itself")
    void gainsTwoLifePerPartyRole() {
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.setLife(player1, 10);

        castShepherd();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts only creatures in your party")
    void ignoresOpponentsPartyCreatures() {
        harness.addToBattlefield(player2, new FaerieMiscreant());
        harness.addToBattlefield(player2, new BoggartBrute());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setLife(player1, 10);

        castShepherd();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    private void castShepherd() {
        harness.setHand(player1, List.of(new ShepherdOfHeroes()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
