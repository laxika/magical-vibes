package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlourishingHunter.class, GrizzlyBears.class})
class FlourishingHunterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains life equal to the greatest toughness among other creatures you control")
    void gainsLifeForGreatestOtherCreatureToughness() {
        GrizzlyBears other = new GrizzlyBears();
        other.setToughness(4);
        harness.addToBattlefield(player1, other);
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new FlourishingHunter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("ETB gains no life when there are no other creatures you control")
    void gainsNoLifeWithoutOtherCreatures() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new FlourishingHunter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }
}
