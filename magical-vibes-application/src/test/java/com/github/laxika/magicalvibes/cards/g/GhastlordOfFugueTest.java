package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhastlordOfFugueTest extends BaseCardTest {

    // ===== Can't be blocked =====

    @Test
    @DisplayName("Ghastlord of Fugue cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = new Permanent(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addAttackingGhastlord(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Combat damage prompts the controller to choose a card from the damaged player's hand")
    void combatDamagePromptsControllerChoice() {
        addAttackingGhastlord(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears())));

        resolveCombat();

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
        harness.setHand(player2, new ArrayList<>(List.of(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears())));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Chosen card is exiled from the damaged player's hand, not put into their graveyard")
    void chosenCardIsExiled() {
        addAttackingGhastlord(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new LightningBolt(), createForest())));

        resolveCombat();
        harness.handleCardChosen(player1, 0); // player1 chooses Lightning Bolt

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("No prompt when the damaged player's hand is empty")
    void noPromptWhenHandEmpty() {
        addAttackingGhastlord(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Helpers =====

    private Permanent addAttackingGhastlord(Player player) {
        Permanent ghastlord = new Permanent(new GhastlordOfFugue());
        ghastlord.setSummoningSick(false);
        ghastlord.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(ghastlord);
        return ghastlord;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card createForest() {
        Card card = new Card();
        card.setName("Forest");
        card.setType(CardType.LAND);
        card.setSubtypes(List.of(CardSubtype.FOREST));
        return card;
    }
}
