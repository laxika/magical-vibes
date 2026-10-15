package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PayManaOrLoseGameAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PactOfTheTitan.class)
class PactOfTheTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pact of the Titan creates a 4/4 red Giant token")
    void createsGiantToken() {
        castPact();

        List<Permanent> giants = findPermanents(player1, "Giant");
        assertThat(giants).hasSize(1);
        assertThat(giants.getFirst().getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(giants.getFirst().getCard().getPower()).isEqualTo(4);
        assertThat(giants.getFirst().getCard().getToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Creates a Giant creature token")
    void createsGiantCreatureToken() {
        castPact();

        Permanent giant = findPermanent(player1, "Giant");
        assertThat(giant.getCard().isToken()).isTrue();
        assertThat(giant.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(giant.getCard().getSubtypes()).containsExactly(CardSubtype.GIANT);
    }

    @Test
    @DisplayName("Schedules the exact payment at the Pact controller's next upkeep")
    void waitsForControllerNextUpkeep() {
        castPact();
        advanceToUpkeep(player2);

        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).singleElement()
                .satisfies(action -> {
                    assertThat(action.playerId()).isEqualTo(player1.getId());
                    assertThat(action.manaCost()).isEqualTo("{4}{R}");
                });
    }

    @Test
    @DisplayName("Paying {4}{R} at the next upkeep avoids losing the game")
    void payingAvoidsLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Being unable to pay at the next upkeep loses the game")
    void beingUnableToPayCausesLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Declining the next-upkeep payment loses the game")
    void decliningCausesLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Five mana without red cannot pay the upkeep obligation")
    void wrongColorManaCausesLoss() {
        castPact();
        reachPactUpkeepPrompt();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Colored mana can pay the generic portion and payment is consumed")
    void coloredManaPaysGenericCost() {
        castPact();
        reachPactUpkeepPrompt();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The paid obligation does not trigger again at later upkeeps")
    void paymentTriggersOnlyOnce() {
        castPact();
        reachPactUpkeepPrompt();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player2);
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Removing the Giant does not remove the payment obligation")
    void paymentSurvivesTokenRemoval() {
        castPact();
        Permanent giant = findPermanent(player1, "Giant");
        giant.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Giant");

        reachPactUpkeepPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void castPact() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PactOfTheTitan()));
        harness.castAndResolveInstant(player1, 0);
    }

    private void reachPactUpkeepPrompt() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
