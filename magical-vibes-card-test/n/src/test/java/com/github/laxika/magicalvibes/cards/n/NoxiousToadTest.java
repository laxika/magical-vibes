package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoxiousToad.class, GrizzlyBears.class})
class NoxiousToadTest extends BaseCardTest {

    @Test
    @DisplayName("When Noxious Toad dies, its death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        setupCombatWhereToadDies();
        resolveCombat();

        harness.assertInGraveyard(player1, "Noxious Toad");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Noxious Toad");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each opponent discard a card")
    void eachOpponentDiscardsACard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        setupCombatWhereToadDies();
        resolveCombat();
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0); // discard the chosen card

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Death trigger does not make its controller discard a card")
    void controllerDoesNotDiscard() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        setupCombatWhereToadDies();
        resolveCombat();
        harness.passBothPriorities(); // Resolve death trigger

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Noxious Toad");
    }

    @Test
    @DisplayName("Death trigger with an empty opponent hand requires no discard")
    void emptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());

        setupCombatWhereToadDies();
        resolveCombat();
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("no cards to discard")).isTrue();
    }

    @Test
    @DisplayName("Opponent chooses which card to discard")
    void opponentChoosesCardToDiscard() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new NoxiousToad()));

        setupCombatWhereToadDies();
        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Noxious Toad");
        harness.assertInGraveyard(player2, "Noxious Toad");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Toad controlled by the defending player makes the attacking player discard")
    void defendingControllersOpponentDiscards() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new NoxiousToad()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent toad = addCreatureReady(player2, new NoxiousToad());
        toad.setBlocking(true);
        toad.addBlockingTarget(0);

        resolveCombat();
        harness.assertInGraveyard(player2, "Noxious Toad");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Noxious Toad");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupCombatWhereToadDies() {
        Permanent toadPerm = addCreatureReady(player1, new NoxiousToad());
        toadPerm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
    }
}
