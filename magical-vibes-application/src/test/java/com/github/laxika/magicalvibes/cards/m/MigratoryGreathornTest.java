package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MigratoryGreathorn.class, Forest.class, GrizzlyBears.class})
class MigratoryGreathornTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating searches for a basic land and puts it onto the battlefield tapped")
    void mutatingSearchesForBasicLandToBattlefieldTapped() {
        Permanent greathorn = addCreatureReady(player1, new MigratoryGreathorn());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), forest));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, greathorn, List.of(greathorn.getCard()), player1.getId()));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mutating another creature does not trigger Migratory Greathorn")
    void anotherCreatureMutatingDoesNotTrigger() {
        addCreatureReady(player1, new MigratoryGreathorn());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, bear, List.of(bear.getCard()), player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
