package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousNecrotitan.class, CopperLonglegs.class})
class RavenousNecrotitanTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature when no opponent has three poison counters")
    void sacrificesCreatureWithoutCorrupted() {
        Permanent support = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, support.getId());

        harness.assertOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertInGraveyard(player1, "Copper Longlegs");
    }

    @Test
    @DisplayName("Does not sacrifice a creature when an opponent has three poison counters")
    void doesNotSacrificeWithCorrupted() {
        harness.addToBattlefield(player1, new CopperLonglegs());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertOnBattlefield(player1, "Copper Longlegs");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Checks corrupted when the ETB trigger resolves")
    void checksCorruptedAtResolution() {
        harness.addToBattlefield(player1, new CopperLonglegs());
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertOnBattlefield(player1, "Copper Longlegs");
    }

    @Test
    @DisplayName("Two opposing poison counters still require sacrifice despite controller having three")
    void controllerPoisonDoesNotEnableCorrupted() {
        Permanent support = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, support.getId());

        harness.assertOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertInGraveyard(player1, "Copper Longlegs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("More than three opposing poison counters prevent sacrifice")
    void doesNotSacrificeAboveThreshold() {
        gd.playerPoisonCounters.put(player2.getId(), 4);
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertNotInGraveyard(player1, "Ravenous Necrotitan");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices itself when it is the controller's only creature")
    void sacrificesItselfWhenAlone() {
        harness.addToBattlefield(player2, new CopperLonglegs());
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertInGraveyard(player1, "Ravenous Necrotitan");
        harness.assertOnBattlefield(player2, "Copper Longlegs");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can choose Necrotitan itself even when another creature is available")
    void canChooseItself() {
        harness.addToBattlefield(player1, new CopperLonglegs());
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ravenous Necrotitan"));

        harness.assertNotOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertInGraveyard(player1, "Ravenous Necrotitan");
        harness.assertOnBattlefield(player1, "Copper Longlegs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Always triggers and requires sacrifice if corrupted is lost before resolution")
    void sacrificesWhenCorruptedIsLostBeforeResolution() {
        Permanent support = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerPoisonCounters.put(player2.getId(), 2);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, support.getId());

        harness.assertOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertInGraveyard(player1, "Copper Longlegs");
        assertThat(gd.stack).isEmpty();
    }
}
