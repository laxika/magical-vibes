package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ApexAltisaur.class, GrizzlyBears.class, Shock.class})
class ApexAltisaurTest extends BaseCardTest {

    @Test
    void entersAndFightsUpToOneOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ApexAltisaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent apex = findPermanent(player1, "Apex Altisaur");
        assertThat(apex.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void enrageFightsAfterApexAltisaurIsDealtDamage() {
        Permanent apex = harness.addToBattlefieldAndReturn(player2, new ApexAltisaur());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, apex.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(apex);
        assertThat(apex.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void doesNotFightWhenThereIsNoOpponentCreature() {
        harness.setHand(player1, List.of(new ApexAltisaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Apex Altisaur");
    }
}
