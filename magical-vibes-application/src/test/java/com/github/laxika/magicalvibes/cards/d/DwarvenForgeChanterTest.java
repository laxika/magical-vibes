package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DwarvenForgeChanterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn (prowess)")
    void noncreatureSpellPumps() {
        Permanent chanter = addChanter();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chanter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chanter)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chanter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chanter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent chanter = addChanter();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, chanter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chanter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they decline to pay 2 life")
    void wardCountersWhenPaymentDeclined() {
        Permanent chanter = addChanter();
        harness.setLife(player2, 20);
        castShockAt(chanter);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Dwarven Forge-Chanter");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Paying 2 life lets an opponent's targeted spell resolve")
    void wardCanBePaid() {
        Permanent chanter = addChanter();
        harness.setLife(player2, 20);
        castShockAt(chanter);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Dwarven Forge-Chanter");
        assertThat(chanter.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addChanter() {
        harness.addToBattlefield(player1, new DwarvenForgeChanter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Dwarven Forge-Chanter");
    }

    private void castShockAt(Permanent chanter) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, chanter.getId());
    }
}
