package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.s.ScavengingOoze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalVessel.class, DoublingSeason.class, ScavengingOoze.class})
class InfernalVesselTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from its first death with two +1/+1 counters as a Demon")
    void returnsAsDemonWithTwoCounters() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InfernalVessel());

        kill(vessel);

        Permanent returned = findPermanent(player1, "Infernal Vessel");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.DEMON);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not return when the Demon dies")
    void doesNotReturnWhenDemonDies() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InfernalVessel());

        kill(vessel);
        Permanent demon = findPermanent(player1, "Infernal Vessel");
        kill(demon);

        harness.assertNotOnBattlefield(player1, "Infernal Vessel");
        harness.assertInGraveyard(player1, "Infernal Vessel");
    }

    @Test
    @DisplayName("The return waits for the death trigger to resolve")
    void returnUsesTheStack() {
        InfernalVessel card = new InfernalVessel();
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, card);

        vessel.setMarkedDamage(vessel.getEffectiveToughness());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Infernal Vessel");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Infernal Vessel").getCard().getId()).isEqualTo(card.getId());
        harness.assertNotInGraveyard(player1, "Infernal Vessel");
    }

    @Test
    @DisplayName("A stolen Vessel returns under its owner's control")
    void returnsToOwnerRatherThanController() {
        InfernalVessel card = new InfernalVessel();
        card.setOwnerId(player2.getId());
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, card);

        kill(vessel);

        harness.assertNotOnBattlefield(player1, "Infernal Vessel");
        Permanent returned = findPermanent(player2, "Infernal Vessel");
        assertThat(returned.getCard().getId()).isEqualTo(card.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.DEMON)).isTrue();
        harness.assertNotInGraveyard(player2, "Infernal Vessel");
    }

    @Test
    @DisplayName("A Vessel that is already a Demon does not trigger on death")
    void alreadyDemonDoesNotReturn() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InfernalVessel());
        vessel.getGrantedSubtypes().add(CardSubtype.DEMON);

        vessel.setMarkedDamage(vessel.getEffectiveToughness());
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Infernal Vessel");
        harness.assertInGraveyard(player1, "Infernal Vessel");
    }

    @Test
    @DisplayName("Doubling Season doubles the counters the returning Vessel enters with")
    void doublingSeasonDoublesReturnCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InfernalVessel());

        kill(vessel);

        Permanent returned = findPermanent(player1, "Infernal Vessel");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exiling the Vessel in response prevents its return")
    void exiledVesselDoesNotReturn() {
        InfernalVessel card = new InfernalVessel();
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, card);
        harness.addToBattlefield(player1, new ScavengingOoze());
        harness.addMana(player1, ManaColor.GREEN, 1);

        vessel.setMarkedDamage(vessel.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Infernal Vessel");
        harness.assertNotInGraveyard(player1, "Infernal Vessel");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    private void kill(Permanent vessel) {
        vessel.setMarkedDamage(vessel.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
