package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrdruunCommando.class, ViashinoFangtail.class})
class OrdruunCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 1 damage dealt to itself")
    void preventsNextDamageToItself() {
        Permanent commando = addCreatureReady(player1, new OrdruunCommando());
        addCreatureReady(player2, new ViashinoFangtail());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, commando.getId());
        harness.passBothPriorities();

        assertThat(commando.getMarkedDamage()).isZero();
        assertThat(commando.getDamagePreventionShield()).isZero();
        harness.assertOnBattlefield(player1, "Ordruun Commando");
    }

    @Test
    @DisplayName("Only prevents damage dealt to itself")
    void doesNotPreventDamageToAnotherCreature() {
        Permanent commando = addCreatureReady(player1, new OrdruunCommando());
        Permanent otherCreature = addCreatureReady(player1, new ViashinoFangtail());
        addCreatureReady(player2, new ViashinoFangtail());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(otherCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(commando.getDamagePreventionShield()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ordruun Commando");
    }

    @Test
    @DisplayName("A consumed shield does not prevent a later damage event")
    void consumedShieldDoesNotPreventLaterDamage() {
        Permanent commando = addCreatureReady(player1, new OrdruunCommando());
        addCreatureReady(player2, new ViashinoFangtail());
        addCreatureReady(player2, new ViashinoFangtail());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, commando.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ordruun Commando");

        harness.activateAbility(player2, 1, null, commando.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ordruun Commando");
        harness.assertInGraveyard(player1, "Ordruun Commando");
    }

    @Test
    @DisplayName("Repeated activations protect against separate damage events without tapping")
    void repeatedActivationsAccumulatePrevention() {
        Permanent commando = addCreatureReady(player1, new OrdruunCommando());
        commando.setSummoningSick(true);
        commando.tap();
        addCreatureReady(player2, new ViashinoFangtail());
        addCreatureReady(player2, new ViashinoFangtail());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, commando.getId());
        harness.passBothPriorities();
        assertThat(commando.getDamagePreventionShield()).isEqualTo(1);

        harness.activateAbility(player2, 1, null, commando.getId());
        harness.passBothPriorities();

        assertThat(commando.getMarkedDamage()).isZero();
        assertThat(commando.getDamagePreventionShield()).isZero();
        harness.assertOnBattlefield(player1, "Ordruun Commando");
    }

    @Test
    @DisplayName("The prevention shield expires at end of turn")
    void shieldExpiresAtEndOfTurn() {
        Permanent commando = addCreatureReady(player1, new OrdruunCommando());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(commando.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(commando.getDamagePreventionShield()).isZero();
    }
}
