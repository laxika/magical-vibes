package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoundaryLandsRanger.class, ColossalDreadmaw.class, Forest.class, GrizzlyBears.class})
class BoundaryLandsRangerTest extends BaseCardTest {

    @Test
    void acceptingMayDiscardsThenDraws() {
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
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
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
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
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentCreatureDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        harness.addToBattlefield(player2, new ColossalDreadmaw());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void conditionIsCheckedAgainWhenAbilityResolves() {
        harness.addToBattlefield(player1, new BoundaryLandsRanger());
        Permanent qualifyingCreature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(qualifyingCreature);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
