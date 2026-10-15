package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisceraSeer.class, RuneclawBear.class})
class VisceraSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature enters scry state with 1 card")
    void sacrificeCreatureTriggersScry() {
        addReadySeer(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        // Bears should be sacrificed
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");

        // Should be in scry state
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scry 1 keeping card on top preserves it")
    void scryKeepOnTop() {
        addReadySeer(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(deck.get(0)).isSameAs(topCard);
    }

    @Test
    @DisplayName("Scry 1 putting card on bottom moves it")
    void scryPutOnBottom() {
        addReadySeer(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.get(0)).isNotSameAs(topCard);
        assertThat(deck.get(deck.size() - 1)).isSameAs(topCard);
    }

    @Test
    @DisplayName("Can sacrifice Viscera Seer to its own ability")
    void canSacrificeItself() {
        addReadySeer(player1);

        harness.activateAbility(player1, 0, null, null);

        // Seer should be sacrificed, ability on stack
        harness.assertNotOnBattlefield(player1, "Viscera Seer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Ability works while tapped (no tap cost)")
    void worksWhileTapped() {
        Permanent seer = addReadySeer(player1);
        seer.tap();
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Auto-sacrifices when only one creature is available")
    void autoSacrificesWhenOnlyOneCreature() {
        addReadySeer(player1);

        harness.activateAbility(player1, 0, null, null);

        // Seer should be auto-sacrificed (only creature available)
        harness.assertNotOnBattlefield(player1, "Viscera Seer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Can activate multiple times per turn with different creatures")
    void canActivateMultipleTimes() {
        addReadySeer(player1);
        harness.addToBattlefield(player1, new RuneclawBear());
        Permanent bears2Perm = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bears2Perm.setSummoningSick(false);

        UUID bears1Id = harness.getPermanentId(player1, "Runeclaw Bear");

        // First activation (3 creatures: Seer + 2 Bears)
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears1Id);
        harness.passBothPriorities();

        // Complete scry
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        // Second activation with the other bears (2 creatures: Seer + 1 Bear)
        UUID bears2Id = bears2Perm.getId();
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears2Id);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        // Both bears should be in graveyard
        long bearsInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Runeclaw Bear"))
                .count();
        assertThat(bearsInGraveyard).isEqualTo(2);
    }

    @Test
    @DisplayName("Self-sacrifice resolves and scries only the controller's library")
    void selfSacrificeResolvesScry() {
        harness.addToBattlefield(player1, new VisceraSeer());
        harness.addToBattlefield(player2, new RuneclawBear());
        Card topCard = new RuneclawBear();
        Card secondCard = new VisceraSeer();
        Card opponentCard = new RuneclawBear();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Viscera Seer");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry with an empty library resolves without a choice")
    void emptyLibraryStillAllowsSacrifice() {
        harness.addToBattlefield(player1, new VisceraSeer());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Viscera Seer");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySeer(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VisceraSeer());
        perm.setSummoningSick(false);
        return perm;
    }
}
