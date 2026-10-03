package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HamletGlutton;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoundaryLandsRanger.class, Forest.class, HamletGlutton.class})
class BoundaryLandsRangerTest extends BaseCardTest {

    @Test
    void acceptingMayDiscardsThenDraws() {
        Card discarded = new BoundaryLandsRanger();
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        harness.addToBattlefield(player1, new HamletGlutton());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void decliningMayDoesNotDiscardOrDraw() {
        Card discarded = new BoundaryLandsRanger();
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        harness.addToBattlefield(player1, new HamletGlutton());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerWithoutAQualifyingCreature() {
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        harness.addToBattlefield(player1, new BoundaryLandsRanger());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentCreatureDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        harness.addToBattlefield(player2, new HamletGlutton());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void conditionIsCheckedAgainWhenAbilityResolves() {
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        Permanent qualifyingCreature = harness.addToBattlefieldAndReturn(player1, new HamletGlutton());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(qualifyingCreature);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingWithAnEmptyHandDoesNotDraw() {
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        harness.addToBattlefield(player1, new HamletGlutton());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void rangerItselfWithExactlyFourPowerQualifies() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        ranger.setPowerModifier(2);
        Card discarded = new Forest();
        Card drawn = new BoundaryLandsRanger();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void threePowerDoesNotQualify() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        ranger.setPowerModifier(1);

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        harness.addToBattlefield(player1, new HamletGlutton());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void losingPowerBeforeResolutionPreventsDiscardAndDraw() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new BoundaryLandsRanger());
        ranger.setPowerModifier(2);
        Card inHand = new Forest();
        Card inLibrary = new BoundaryLandsRanger();
        harness.setHand(player1, List.of(inHand));
        harness.setLibrary(player1, List.of(inLibrary));

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        ranger.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(inLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(activePlayer);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
