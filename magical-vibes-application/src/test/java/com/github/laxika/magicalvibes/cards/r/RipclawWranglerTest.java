package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GuidelightOptimizer;
import com.github.laxika.magicalvibes.cards.n.NestingBot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RipclawWrangler.class, GuidelightOptimizer.class, NestingBot.class})
class RipclawWranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes each opponent discard a card")
    void enteringTheBattlefieldMakesEachOpponentDiscard() {
        harness.setHand(player2, List.of(new GuidelightOptimizer()));
        castRipclawWrangler();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Guidelight Optimizer");
    }

    @Test
    @DisplayName("Crew 2 animates Ripclaw Wrangler and taps the chosen creature")
    void crewAnimatesRipclawWrangler() {
        Permanent wrangler = addCreatureReady(player1, new RipclawWrangler());
        Permanent crew = addCreatureReady(player1, new GuidelightOptimizer());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, wrangler)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The opponent chooses exactly one card and the controller keeps their hand")
    void opponentChoosesOneCard() {
        GuidelightOptimizer kept = new GuidelightOptimizer();
        RipclawWrangler discarded = new RipclawWrangler();
        harness.setHand(player2, List.of(kept, discarded));
        castRipclawWrangler();
        GuidelightOptimizer controllerCard = new GuidelightOptimizer();
        harness.setHand(player1, List.of(controllerCard));

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.DiscardChoice.class,
                        choice -> assertThat(choice.playerId()).isEqualTo(player2.getId()));
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent with an empty hand requires no discard choice")
    void emptyOpponentHandDoesNotPrompt() {
        harness.setHand(player2, List.of());
        castRipclawWrangler();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Ripclaw Wrangler");
    }

    @Test
    @DisplayName("An opposing controller makes the other player discard")
    void opponentIsRelativeToTriggerController() {
        GuidelightOptimizer discarded = new GuidelightOptimizer();
        harness.setHand(player1, List.of(discarded));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new RipclawWrangler(), "{3}{B}");

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.DiscardChoice.class,
                        choice -> assertThat(choice.playerId()).isEqualTo(player1.getId()));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew a newly entered Vehicle")
    void summoningSickCreatureCanCrew() {
        Permanent wrangler = harness.addToBattlefieldAndReturn(player1, new RipclawWrangler());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GuidelightOptimizer());
        wrangler.setSummoningSick(true);
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, wrangler)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, wrangler)).isTrue();
        assertThat(wrangler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crew animation ends at the end of the turn")
    void crewAnimationEndsAtEndOfTurn() {
        Permanent wrangler = addCreatureReady(player1, new RipclawWrangler());
        addCreatureReady(player1, new GuidelightOptimizer());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, wrangler)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, wrangler)).isFalse();
        harness.assertOnBattlefield(player1, "Ripclaw Wrangler");
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay the crew cost")
    void unavailableCreaturesCannotCrew() {
        Permanent wrangler = addCreatureReady(player1, new RipclawWrangler());
        Permanent tappedCrew = addCreatureReady(player1, new GuidelightOptimizer());
        tappedCrew.tap();
        addCreatureReady(player2, new GuidelightOptimizer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");

        assertThat(gqs.isCreature(gd, wrangler)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Crew 2 combines the power of two creatures")
    void twoOnePowerCreaturesCanCrew() {
        Permanent wrangler = addCreatureReady(player1, new RipclawWrangler());
        Permanent firstCrew = addCreatureReady(player1, new NestingBot());
        Permanent secondCrew = addCreatureReady(player1, new NestingBot());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, wrangler)).isTrue();
    }

    @Test
    @DisplayName("A single creature with power one cannot pay Crew 2")
    void insufficientPowerCannotCrew() {
        Permanent wrangler = addCreatureReady(player1, new RipclawWrangler());
        Permanent crew = addCreatureReady(player1, new NestingBot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");

        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, wrangler)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castRipclawWrangler() {
        harness.castFromHand(player1, new RipclawWrangler(), "{3}{B}");
    }
}
