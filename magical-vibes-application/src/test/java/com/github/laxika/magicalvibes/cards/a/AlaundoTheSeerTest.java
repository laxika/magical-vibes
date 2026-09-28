package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlaundoTheSeer.class, GrizzlyBears.class})
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
        GrizzlyBears chosen = new GrizzlyBears();
        GrizzlyBears other = new GrizzlyBears();
        harness.setExile(player1, List.of(other));
        gd.exiledCardTimeCounters.put(other.getId(), 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(chosen));

        activateAndChoose(alaundo);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent cast = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, cast, Keyword.HASTE)).isTrue();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(other.getId());
    }

    private Permanent alaundo() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new AlaundoTheSeer());
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private void activateAndChoose(Permanent alaundo) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(alaundo), null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);
    }
}
