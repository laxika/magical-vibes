package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuinRat.class, GrizzlyBears.class, WrathOfGod.class})
class RuinRatTest extends BaseCardTest {

    /** Wraths the board so Ruin Rat (player1's only creature) dies, firing its ON_DEATH trigger. */
    private void wrathToKillRuinRat() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Wrath resolves — Ruin Rat dies, graveyard-target choice presented
    }

    @Test
    @DisplayName("When Ruin Rat dies, it exiles a targeted card from an opponent's graveyard")
    void deathExilesOpponentGraveyardCard() {
        harness.addToBattlefield(player1, new RuinRat());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        wrathToKillRuinRat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities(); // resolve the death triggered ability

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Only an opponent's graveyard cards are legal targets — the controller's own is excluded")
    void ownGraveyardCardNotTargetable() {
        harness.addToBattlefield(player1, new RuinRat());
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        wrathToKillRuinRat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(opponentCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(ownCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        // The controller's own graveyard card is untouched.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ownCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(opponentCard.getId()));
    }

    @Test
    @DisplayName("With no card in any opponent's graveyard the death trigger presents no choice")
    void noOpponentGraveyardCardNoChoice() {
        harness.addToBattlefield(player1, new RuinRat());
        // Only the controller's own graveyard has a card → no legal opponent target.
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        wrathToKillRuinRat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    void canTargetCreatureThatDiesAtTheSameTime() {
        harness.addToBattlefield(player1, new RuinRat());
        Card opponentCreature = new GrizzlyBears();
        harness.addToBattlefield(player2, opponentCreature);

        wrathToKillRuinRat();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(opponentCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCreature);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void mustChooseExactlyOneCardAndCanExileNoncreature() {
        harness.addToBattlefield(player1, new RuinRat());
        Card target = new WrathOfGod();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target, other));

        wrathToKillRuinRat();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(target.getId(), other.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void targetLeavingGraveyardDoesNotExileAnotherCard() {
        harness.addToBattlefield(player1, new RuinRat());
        Card target = new GrizzlyBears();
        Card other = new WrathOfGod();
        harness.setGraveyard(player2, List.of(target, other));

        wrathToKillRuinRat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(other));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Wrath of God");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void deathtouchKillsTougherBlockerAndDeathTriggerCanExileIt() {
        addCreatureReady(player1, new RuinRat());
        Card blocker = new GrizzlyBears();
        addCreatureReady(player2, blocker);

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Ruin Rat");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blocker.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(blocker);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
