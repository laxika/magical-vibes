package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GnollHunter;
import com.github.laxika.magicalvibes.cards.e.ErrantEphemeron;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlaundoTheSeer.class, GrizzlyBears.class, GnollHunter.class, ErrantEphemeron.class, Island.class})
class AlaundoTheSeerTest extends BaseCardTest {

    @Test
    void exilesDrawnHandChoiceWithManaValueCountersAndAdvancesOtherOwnedCards() {
        Permanent alaundo = alaundo();
        GrizzlyBears chosen = new GrizzlyBears();
        GrizzlyBears otherOwned = new GrizzlyBears();
        GrizzlyBears opponentOwned = new GrizzlyBears();
        harness.setExile(player1, List.of(otherOwned));
        harness.setExile(player2, List.of(opponentOwned));
        gd.exiledCardTimeCounters.put(otherOwned.getId(), 2);
        gd.exiledCardTimeCounters.put(opponentOwned.getId(), 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(chosen));

        activateAndChoose(alaundo);

        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(chosen.getId(), 2)
                .containsEntry(otherOwned.getId(), 1)
                .containsEntry(opponentOwned.getId(), 2);
        assertThat(gd.exiledCardsWithNonSuspendTimeCounters).containsExactly(chosen.getId());
    }

    @Test
    void alaundoTimeCountersDoNotTriggerSuspendUpkeepRemoval() {
        Permanent alaundo = alaundo();
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(chosen));

        activateAndChoose(alaundo);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(chosen.getId(), 2);
    }

    @Test
    void removingTheLastOtherCounterOffersFreeCastWithHaste() {
        Permanent alaundo = alaundo();
        GrizzlyBears other = new GrizzlyBears();
        harness.setHand(player1, List.of(other));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        activateAndChoose(alaundo);
        harness.performUntapStep(player1);
        activateAndChoose(alaundo);
        harness.performUntapStep(player1);
        activateAndChoose(alaundo);
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        Permanent cast = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, cast, Keyword.HASTE)).isTrue();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(other.getId());
    }

    @Test
    void lastCounterCreatesATriggerThatPlayersCanRespondTo() {
        Permanent alaundo = alaundo();
        GnollHunter exiled = new GnollHunter();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new GnollHunter(), new GnollHunter(), new GnollHunter()));
        activateAndChoose(alaundo);
        harness.performUntapStep(player1);
        activateAndChoose(alaundo);
        harness.performUntapStep(player1);
        activateAndChoose(alaundo);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void unrelatedExiledCardDoesNotGainAFreeCastWhenItsLastCounterIsRemoved() {
        Permanent alaundo = alaundo();
        GnollHunter unrelated = new GnollHunter();
        harness.setExile(player1, List.of(unrelated));
        gd.exiledCardTimeCounters.put(unrelated.getId(), 1);
        gd.exiledCardsWithNonSuspendTimeCounters.add(unrelated.getId());
        harness.setHand(player1, List.of(new GnollHunter()));
        harness.setLibrary(player1, List.of(new GnollHunter()));

        activateAndChoose(alaundo);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(unrelated.getId())).isNotNull();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(unrelated.getId());
    }

    @Test
    void nativeSuspendStillRemovesCountersAtUpkeepWhenExiledByAlaundo() {
        Permanent alaundo = alaundo();
        ErrantEphemeron exiled = new ErrantEphemeron();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new GnollHunter()));
        activateAndChoose(alaundo);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(exiled.getId(), 6);
    }

    @Test
    void nativeSuspendAndGrantedCastingAbilityTriggerIndependently() {
        Permanent alaundo = alaundo();
        ErrantEphemeron exiled = new ErrantEphemeron();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new GnollHunter(), new GnollHunter()));
        activateAndChoose(alaundo);
        gd.exiledCardTimeCounters.put(exiled.getId(), 1);
        harness.performUntapStep(player1);

        activateAndChoose(alaundo);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningTheFreeCastLeavesTheCardExiledWithoutCounters() {
        Permanent alaundo = alaundo();
        GnollHunter exiled = new GnollHunter();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new GnollHunter(), new GnollHunter(), new GnollHunter()));
        activateAndChoose(alaundo);
        harness.performUntapStep(player1);
        activateAndChoose(alaundo);
        harness.performUntapStep(player1);
        activateAndChoose(alaundo);
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(exiled.getId());
        harness.assertNotOnBattlefield(player1, "Gnoll Hunter");
    }

    @Test
    void decliningCastingPreservesTheAbilityForALaterLastCounter() {
        Permanent alaundo = alaundo();
        GnollHunter exiled = new GnollHunter();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new GnollHunter(), new GnollHunter(), new GnollHunter()));
        activateAndChoose(alaundo);
        gd.exiledCardTimeCounters.put(exiled.getId(), 1);
        harness.performUntapStep(player1);
        activateAndChoose(alaundo);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleMayAbilityChosen(player1, false));
        gd.exiledCardTimeCounters.put(exiled.getId(), 1);
        harness.performUntapStep(player1);

        activateAndChoose(alaundo);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void exilingALandDoesNotOfferACastAndStillAdvancesOtherCards() {
        Permanent alaundo = alaundo();
        Island land = new Island();
        GnollHunter other = new GnollHunter();
        harness.setExile(player1, List.of(other));
        gd.exiledCardTimeCounters.put(other.getId(), 2);
        gd.exiledCardsWithNonSuspendTimeCounters.add(other.getId());
        harness.setHand(player1, List.of(land));
        harness.setLibrary(player1, List.of(new GnollHunter()));

        activateAndChoose(alaundo);

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(land.getId()).containsEntry(other.getId(), 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void freeCastHasteExpiresAtEndOfTurn() {
        Permanent alaundo = alaundo();
        GnollHunter exiled = new GnollHunter();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new GnollHunter(), new GnollHunter(), new GnollHunter()));
        activateAndChoose(alaundo);
        harness.performUntapStep(player1);
        activateAndChoose(alaundo);
        harness.performUntapStep(player1);
        activateAndChoose(alaundo);
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });
        Permanent cast = findPermanent(player1, "Gnoll Hunter");
        assertThat(gqs.hasKeyword(gd, cast, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, cast, Keyword.HASTE)).isFalse();
    }

    private Permanent alaundo() {
        Permanent permanent = addCreatureReady(player1, new AlaundoTheSeer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private void activateAndChoose(Permanent alaundo) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(alaundo), null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.handleCardChosen(player1, 0));
    }
}
