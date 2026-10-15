package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncientCrab;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuratorOfMysteries.class, Censor.class, AncientCrab.class, TormentingVoice.class})
class CuratorOfMysteriesTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card triggers scry 1")
    void cyclingAnotherCardTriggersScry() {
        harness.addToBattlefield(player1, new CuratorOfMysteries());
        // A second cycling card to cycle — cycling is a discard, so it triggers the scry.
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new AncientCrab(), new AncientCrab()));
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
        harness.setLibrary(player1, List.of(new AncientCrab(), new AncientCrab()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the scry trigger — it sits above the cycling draw

        // Keep the scryed card on top, then let the cycling draw resolve.
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInHand(player1, "Ancient Crab");
        harness.assertInGraveyard(player1, "Censor");
    }

    @Test
    @DisplayName("Cycling Curator of Mysteries itself does not scry")
    void cyclingSelfDoesNotScry() {
        // The ability only functions on the battlefield; cycling Curator from hand can't trigger it.
        harness.setHand(player1, List.of(new CuratorOfMysteries()));
        harness.setLibrary(player1, List.of(new AncientCrab()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInHand(player1, "Ancient Crab");
        harness.assertInGraveyard(player1, "Curator of Mysteries");
    }

    @Test
    @DisplayName("Discarding a noncycling card as a spell cost triggers scry")
    void ordinaryDiscardTriggersScry() {
        harness.addToBattlefield(player1, new CuratorOfMysteries());
        harness.setHand(player1, List.of(new TormentingVoice(), new AncientCrab()));
        AncientCrab top = new AncientCrab();
        Censor second = new Censor();
        CuratorOfMysteries third = new CuratorOfMysteries();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Ancient Crab");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent cycling does not trigger Curator")
    void opponentCyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new CuratorOfMysteries());
        harness.setHand(player2, List.of(new Censor()));
        AncientCrab drawn = new AncientCrab();
        harness.setLibrary(player2, List.of(drawn));
        harness.setLibrary(player1, List.of(new AncientCrab()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling another Curator triggers the battlefield Curator once")
    void cyclingAnotherCuratorTriggersOnce() {
        harness.addToBattlefield(player1, new CuratorOfMysteries());
        harness.setHand(player1, List.of(new CuratorOfMysteries()));
        harness.setLibrary(player1, List.of(new AncientCrab()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ancient Crab");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
