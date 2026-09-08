package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RavenousNecrotitanTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature when no opponent has three poison counters")
    void sacrificesCreatureWithoutCorrupted() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not sacrifice a creature when an opponent has three poison counters")
    void doesNotSacrificeWithCorrupted() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Checks corrupted when the ETB trigger resolves")
    void checksCorruptedAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RavenousNecrotitan()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Necrotitan");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
