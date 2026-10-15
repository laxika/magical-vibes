package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({SazhKatzroy.class, BirdsOfParadise.class, Forest.class, GrizzlyBears.class, Mountain.class})
class SazhKatzroyTest extends BaseCardTest {

    @Test
    @DisplayName("Sazh's enter-the-battlefield ability may search for a Bird or basic land")
    void entersMaySearchForBirdOrBasicLand() {
        BirdsOfParadise bird = new BirdsOfParadise();
        Forest forest = new Forest();
        GrizzlyBears nonmatching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bird, forest, nonmatching));
        castSazh();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(bird, forest);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(bird);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, nonmatching);
    }

    @Test
    @DisplayName("Declining Sazh's enter-the-battlefield search does nothing")
    void mayDeclineSearch() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castSazh();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Attacking puts a counter on a target creature and then doubles its counters")
    void attackPutsAndDoublesCountersOnTargetCreature() {
        Permanent sazh = addCreatureReady(player1, new SazhKatzroy());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        Permanent land = gd.playerBattlefields.get(player1.getId()).get(2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(sazh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castSazh() {
        harness.castFromHand(player1, new SazhKatzroy(), "{3}{G}");
    }

    @Test
    void searchCanPutABasicLandIntoHandAndRevealIt() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castSazh();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("reveals Forest and puts it into their hand")).isTrue();
    }

    @Test
    void searchCanFailToFindEvenWhenAMatchingCardExists() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castSazh();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingSearchWithAnEmptyLibraryCompletes() {
        harness.setLibrary(player1, List.of());
        castSazh();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackCanTargetSazhAndAddsTwoCountersStartingFromZero() {
        Permanent sazh = addCreatureReady(player1, new SazhKatzroy());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, sazh.getId());
        resolveAllTriggers();

        assertThat(sazh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackCanTargetAnOpponentsCreatureAndLeavesOtherCounterTypesUnchanged() {
        addCreatureReady(player1, new SazhKatzroy());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void attackDoesNotMoveCountersToAnotherCreatureWhenTargetLeaves() {
        Permanent sazh = addCreatureReady(player1, new SazhKatzroy());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sazh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

}
