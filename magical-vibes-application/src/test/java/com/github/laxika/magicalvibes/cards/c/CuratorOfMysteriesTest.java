package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CuratorOfMysteriesTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card triggers scry 1")
    void cyclingAnotherCardTriggersScry() {
        harness.addToBattlefield(player1, new CuratorOfMysteries());
        // A second cycling card to cycle — cycling is a discard, so it triggers the scry.
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scry resolves before the cycling draw completes")
    void scryThenCyclingDrawResolves() {
        harness.addToBattlefield(player1, new CuratorOfMysteries());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the scry trigger — it sits above the cycling draw

        // Keep the scryed card on top, then let the cycling draw resolve.
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Censor");
    }

    @Test
    @DisplayName("Cycling Curator of Mysteries itself does not scry")
    void cyclingSelfDoesNotScry() {
        // The ability only functions on the battlefield; cycling Curator from hand can't trigger it.
        harness.setHand(player1, List.of(new CuratorOfMysteries()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Curator of Mysteries");
    }
}
