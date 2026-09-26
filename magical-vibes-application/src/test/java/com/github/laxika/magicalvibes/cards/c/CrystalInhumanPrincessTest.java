package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrystalInhumanPrincess.class, GrizzlyBears.class, Opt.class})
class CrystalInhumanPrincessTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of colors of a noncreature spell")
    void dealsDamageForEachSpellColor() {
        harness.addToBattlefield(player1, new CrystalInhumanPrincess());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Creature spells do not trigger Crystal")
    void creatureSpellsDoNotTrigger() {
        harness.addToBattlefield(player1, new CrystalInhumanPrincess());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping Crystal adds one mana of a chosen color")
    void tapsForChosenColor() {
        addCreatureReady(player1, new CrystalInhumanPrincess());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
