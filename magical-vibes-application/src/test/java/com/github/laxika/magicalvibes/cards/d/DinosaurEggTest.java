package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DinosaurEgg.class, HillGiant.class, GrizzlyBears.class})
class DinosaurEggTest extends BaseCardTest {

    @Test
    @DisplayName("When Dinosaur Egg dies, it may discover using its toughness")
    void mayDiscoverUsingToughnessAtDeath() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DinosaurEgg());
        egg.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(discovered));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(discovered);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    @DisplayName("Dinosaur Egg's death ability may be declined")
    void mayDeclineDeathAbility() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DinosaurEgg());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
