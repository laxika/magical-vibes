package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DinosaurEgg.class, HillGiant.class, GrizzlyBears.class, GloriousAnthem.class})
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

        harness.handleCardChosen(player1, -1);

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

    @Test
    void evolvesWhenEnteringCreatureHasGreaterPowerOnly() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DinosaurEgg());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotEvolveForEqualStats() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DinosaurEgg());

        harness.castFromHand(player1, new DinosaurEgg(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void evolveRechecksComparisonWhenResolving() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DinosaurEgg());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        egg.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void discoverSkipsCardsAboveUnmodifiedToughnessAndCanCastForFree() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DinosaurEgg());
        HillGiant skipped = new HillGiant();
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(skipped, discovered));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(skipped);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
    }

    @Test
    void discoverUsesToughnessIncludingStaticBoostAtDeath() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DinosaurEgg());
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(discovered));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void discoveringWithNoQualifyingCardReturnsCardsToLibrary() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DinosaurEgg());
        HillGiant tooExpensive = new HillGiant();
        harness.setLibrary(player1, List.of(tooExpensive));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tooExpensive);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(tooExpensive);
    }

    @Test
    void discoveringWithEmptyLibraryFinishesWithoutAChoice() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DinosaurEgg());
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
