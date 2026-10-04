package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravenAbomination.class, FrilledSandwalla.class, Forest.class})
class GravenAbominationTest extends BaseCardTest {

    private Permanent addReadyAttacker() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new GravenAbomination());
        abomination.setSummoningSick(false);
        return abomination;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Attacking exiles a targeted card from the defending player's graveyard")
    void attackExilesDefendingPlayerGraveyardCard() {
        addReadyAttacker();
        Card bears = new FrilledSandwalla();
        harness.setGraveyard(player2, List.of(bears));

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("A card in the attacker's own graveyard is not a legal target")
    void ownGraveyardCardNotTargetable() {
        addReadyAttacker();
        Card ownCard = new FrilledSandwalla();
        Card opponentCard = new FrilledSandwalla();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(opponentCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(ownCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ownCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(opponentCard.getId()));
    }

    @Test
    @DisplayName("Empty defending graveyard produces no target choice")
    void emptyDefendingGraveyardNoChoice() {
        addReadyAttacker();
        harness.setGraveyard(player1, List.of(new FrilledSandwalla()));

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A legal graveyard target must be chosen; the exile is not optional")
    void cannotDeclineTargetSelection() {
        addReadyAttacker();
        Card target = new FrilledSandwalla();
        harness.setGraveyard(player2, List.of(target));
        declareAttack();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Only the chosen card is exiled, and noncreature cards are legal targets")
    void onlyChosenNoncreatureCardIsExiled() {
        addReadyAttacker();
        Card forest = new Forest();
        Card creature = new FrilledSandwalla();
        harness.setGraveyard(player2, List.of(forest, creature));

        declareAttack();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("The attack trigger resolves even if its source leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent abomination = addReadyAttacker();
        Card target = new FrilledSandwalla();
        harness.setGraveyard(player2, List.of(target));
        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(abomination);
        harness.setGraveyard(player1, List.of(abomination.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not exiled and no new target is chosen")
    void targetLeavingGraveyardMakesTriggerIneffective() {
        addReadyAttacker();
        Card target = new FrilledSandwalla();
        Card remaining = new Forest();
        harness.setGraveyard(player2, List.of(target, remaining));
        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
