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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerenOfClanNelToth.class, GrizzlyBears.class, HillGiant.class})
class MerenOfClanNelTothTest extends BaseCardTest {

    @Test
    @DisplayName("gets an experience counter when another creature you control dies")
    void getsExperienceCounterWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("does not get an experience counter when Meren itself dies")
    void doesNotGetExperienceCounterWhenMerenDies() {
        Permanent meren = harness.addToBattlefieldAndReturn(player1, new MerenOfClanNelToth());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, meren));

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("returns a targeted low-mana-value creature to the battlefield")
    void returnsLowManaValueCreatureToBattlefield() {
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        Card target = putMerenTargetInGraveyard(new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        resolveMerenEndStep(target);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("returns a targeted high-mana-value creature to hand")
    void returnsHighManaValueCreatureToHand() {
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        Card target = putMerenTargetInGraveyard(new HillGiant());
        gd.playerExperienceCounters.put(player1.getId(), 0);

        resolveMerenEndStep(target);

        harness.assertInHand(player1, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    private Card putMerenTargetInGraveyard(Card card) {
        harness.setGraveyard(player1, List.of(card));
        return card;
    }

    private void resolveMerenEndStep(Card target) {
        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
