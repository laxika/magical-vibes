package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThrashOfRaptors;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WakeningSunsAvatarTest extends BaseCardTest {

    

    @Test
    @DisplayName("When cast from hand, all non-Dinosaur creatures are destroyed")
    void castFromHandDestroysNonDinosaurs() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WakeningSunsAvatar()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("When cast from hand, Dinosaur creatures survive")
    void castFromHandSparesDinosaurs() {
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player2, new ThrashOfRaptors());

        harness.setHand(player1, List.of(new WakeningSunsAvatar()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thrash of Raptors");
        harness.assertOnBattlefield(player2, "Thrash of Raptors");
    }

    @Test
    @DisplayName("Wakening Sun's Avatar itself survives its own ETB since it is a Dinosaur")
    void avatarItselfsurvivestBecauseItIsADinosaur() {
        harness.setHand(player1, List.of(new WakeningSunsAvatar()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wakening Sun's Avatar");
    }

    @Test
    @DisplayName("When entering not from hand, non-Dinosaur creatures are not destroyed")
    void enteringNotFromHandDoesNotDestroyCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new WakeningSunsAvatar()));
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
