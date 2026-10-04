package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Manamorphose;
import com.github.laxika.magicalvibes.cards.s.ScuzzbackScrapper;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhastlordOfFugue.class, ScuzzbackScrapper.class, Manamorphose.class, Forest.class})
class GhastlordOfFugueTest extends BaseCardTest {

    @Test
    @DisplayName("Ghastlord of Fugue cannot be blocked")
    void cannotBeBlocked() {
        harness.addToBattlefield(player2, new ScuzzbackScrapper());

        addAttackingGhastlord(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Combat damage prompts the controller to choose a card from the damaged player's hand")
    void combatDamagePromptsControllerChoice() {
        addAttackingGhastlord(player1);
        harness.setHand(player2, List.of(new ScuzzbackScrapper()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        // The Ghastlord's controller (player1) makes the choice, targeting player2's hand.
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.exileMode()).isTrue();
    }

    @Test
    @DisplayName("Any card type can be chosen — the controller may exile a creature")
    void controllerCanChooseAnyCard() {
        addAttackingGhastlord(player1);
        // Only a creature in hand — with no type restriction it is still a valid choice.
        harness.setHand(player2, List.of(new ScuzzbackScrapper()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Chosen card is exiled from the damaged player's hand, not put into their graveyard")
    void chosenCardIsExiled() {
        addAttackingGhastlord(player1);
        harness.setHand(player2, List.of(new Manamorphose(), new Forest()));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player1, 0); // player1 chooses Manamorphose

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Manamorphose"));
        harness.assertNotInGraveyard(player2, "Manamorphose");
        harness.assertNotInHand(player2, "Manamorphose");
    }

    @Test
    @DisplayName("No prompt when the damaged player's hand is empty")
    void noPromptWhenHandEmpty() {
        addAttackingGhastlord(player1);
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A land can be chosen and only the selected card is exiled")
    void canExileLandWithoutExilingOtherCards() {
        addAttackingGhastlord(player1);
        Forest forest = new Forest();
        Manamorphose other = new Manamorphose();
        harness.setHand(player2, List.of(other, forest));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The damaged player cannot make the controller's choice")
    void damagedPlayerCannotChooseCard() {
        addAttackingGhastlord(player1);
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a card is mandatory when the damaged player has cards")
    void cannotDeclineExileChoice() {
        addAttackingGhastlord(player1);
        Forest forest = new Forest();
        harness.setHand(player2, List.of(forest));

        resolveCombatAndTrigger();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Player two's Ghastlord exiles a card from player one's hand")
    void opposingControllerChoosesFromDamagedPlayersHand() {
        addAttackingGhastlord(player2);
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));
        harness.setHand(player2, List.of(new Manamorphose()));

        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private Permanent addAttackingGhastlord(Player player) {
        Permanent ghastlord = harness.addToBattlefieldAndReturn(player, new GhastlordOfFugue());
        ghastlord.setSummoningSick(false);
        ghastlord.setAttacking(true);
        return ghastlord;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities(); // resolve what combat damage triggered
    }

}
