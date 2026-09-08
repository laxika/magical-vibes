package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConspiracyTheorist.class, GrizzlyBears.class, Shock.class})
class ConspiracyTheoristTest extends BaseCardTest {

    @Test
    @DisplayName("attacking may pay to discard and draw")
    void attackingMayPayToDiscardAndDraw() {
        addCreatureReady(player1, new ConspiracyTheorist());
        Card discarded = new Shock();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(
                com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(
                com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS));
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("discard trigger exiles a discarded nonland and permits casting it this turn")
    void discardTriggerExilesAndPermitsCastingThisTurn() {
        Permanent source = addCreatureReady(player1, new ConspiracyTheorist());
        Card discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(discarded);

        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBeforeCast = gd.getLife(player2.getId());
        harness.castFromExile(player1, discarded.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeCast - 2);
    }
}
