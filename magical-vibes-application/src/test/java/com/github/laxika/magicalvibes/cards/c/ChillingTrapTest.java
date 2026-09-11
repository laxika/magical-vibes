package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AetherAdept;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChillingTrap.class, AetherAdept.class, GrizzlyBears.class})
class ChillingTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -4/-0 until end of turn")
    void givesTargetCreatureMinusFourPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChillingTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws a card when you control a Wizard")
    void drawsCardWithWizard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AetherAdept());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ChillingTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw a card without a Wizard")
    void doesNotDrawCardWithoutWizard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ChillingTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
