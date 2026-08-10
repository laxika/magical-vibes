package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReiverDemonTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand, destroys nonartifact, nonblack creatures and they cannot be regenerated")
    void castFromHandDestroysNonartifactNonblackCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setRegenerationShield(1);
        harness.addToBattlefield(player1, new AlloyMyr());
        harness.addToBattlefield(player2, new ScatheZombies());

        harness.setHand(player1, List.of(new ReiverDemon()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Alloy Myr");
        harness.assertOnBattlefield(player2, "Scathe Zombies");
        harness.assertOnBattlefield(player1, "Reiver Demon");
    }

    @Test
    @DisplayName("When it enters without being cast from hand, its ability does not destroy creatures")
    void enteringWithoutBeingCastFromHandDoesNotDestroyCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new ReiverDemon()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
