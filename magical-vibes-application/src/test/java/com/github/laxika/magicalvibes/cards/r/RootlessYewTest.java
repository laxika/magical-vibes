package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFrost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RootlessYewTest extends BaseCardTest {

    @Test
    @DisplayName("Its death trigger searches for a creature with power or toughness 6 or greater")
    void deathTriggerSearchesByPowerOrToughness() {
        AvatarOfMight highPower = new AvatarOfMight();
        WallOfFrost highToughness = new WallOfFrost();
        GrizzlyBears tooSmall = new GrizzlyBears();
        Forest nonCreature = new Forest();
        harness.setLibrary(player1, List.of(highPower, highToughness, tooSmall, nonCreature));

        Permanent rootlessYew = harness.addToBattlefieldAndReturn(player1, new RootlessYew());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rootlessYew));

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(highPower, highToughness);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsAnyOf(highPower, highToughness);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Its death trigger has no search when no qualifying creature is in the library")
    void deathTriggerDoesNotOfferNonQualifyingCards() {
        GrizzlyBears tooSmall = new GrizzlyBears();
        Forest nonCreature = new Forest();
        harness.setLibrary(player1, List.of(tooSmall, nonCreature));

        Permanent rootlessYew = harness.addToBattlefieldAndReturn(player1, new RootlessYew());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rootlessYew));

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tooSmall, nonCreature);
    }
}
