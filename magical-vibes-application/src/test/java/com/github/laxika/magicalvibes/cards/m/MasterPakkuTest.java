package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterPakku.class, AirbendingLesson.class, GrizzlyBears.class})
class MasterPakkuTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped mills target player for each Lesson in its controller's graveyard")
    void becomingTappedMillsForLessonsInControllerGraveyard() {
        Permanent pakku = harness.addToBattlefieldAndReturn(player1, new MasterPakku());
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new AirbendingLesson()));
        harness.setLibrary(player2, libraryWithFiveCards());

        tap(pakku);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Tapping another permanent does not trigger Master Pakku")
    void tappingAnotherPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MasterPakku());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(bears);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears());
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });
    }
}
