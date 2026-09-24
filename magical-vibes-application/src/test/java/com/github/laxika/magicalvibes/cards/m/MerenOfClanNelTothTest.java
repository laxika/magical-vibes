package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({MerenOfClanNelToth.class, GrizzlyBears.class, HillGiant.class})
class MerenOfClanNelTothTest extends BaseCardTest {

    @Test
    void gainsExperienceWhenAnotherCreatureYouControlDies() {
        addMeren();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void returnsTargetToBattlefieldWhenManaValueIsWithinExperience() {
        addMeren();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsTargetToHandWhenManaValueExceedsExperience() {
        addMeren();
        Card target = new HillGiant();
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep(player1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Hill Giant");
    }

    private Permanent addMeren() {
        return harness.addToBattlefieldAndReturn(player1, new MerenOfClanNelToth());
    }

    private void chooseTarget(Card target) {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
