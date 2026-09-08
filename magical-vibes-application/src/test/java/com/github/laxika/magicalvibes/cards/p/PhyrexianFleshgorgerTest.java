package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianFleshgorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Prototype ward costs life equal to the prototype power")
    void prototypeWardUsesPrototypePower() {
        Permanent fleshgorger = castPrototype();
        harness.setLife(player2, 20);
        castShockAtFleshgorger(fleshgorger);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
    }

    @Test
    @DisplayName("Normal ward costs life equal to the full power")
    void normalWardUsesNormalPower() {
        Permanent fleshgorger = castNormally();
        harness.setLife(player2, 20);
        castShockAtFleshgorger(fleshgorger);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
    }

    @Test
    @DisplayName("Declining ward counters the targeted spell")
    void decliningWardCountersSpell() {
        Permanent fleshgorger = castPrototype();
        harness.setLife(player2, 20);
        castShockAtFleshgorger(fleshgorger);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward counters the spell immediately when its controller cannot pay")
    void wardCountersWhenControllerCannotPay() {
        Permanent fleshgorger = castPrototype();
        harness.setLife(player2, 2);
        castShockAtFleshgorger(fleshgorger);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Phyrexian Fleshgorger");
        harness.assertInGraveyard(player2, "Shock");
    }

    private Permanent castNormally() {
        harness.setHand(player1, List.of(new PhyrexianFleshgorger()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Phyrexian Fleshgorger");
    }

    private Permanent castPrototype() {
        harness.setHand(player1, List.of(new PhyrexianFleshgorger()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();
        Permanent fleshgorger = findPermanent(player1, "Phyrexian Fleshgorger");
        assertThat(gqs.getEffectivePower(gd, fleshgorger)).isEqualTo(3);
        return fleshgorger;
    }

    private void castShockAtFleshgorger(Permanent fleshgorger) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, fleshgorger.getId());
    }
}
