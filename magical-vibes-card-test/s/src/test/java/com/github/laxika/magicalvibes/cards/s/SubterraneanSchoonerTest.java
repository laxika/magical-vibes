package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronpawAspirant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SubterraneanSchooner.class, IronpawAspirant.class, Forest.class})
class SubterraneanSchoonerTest extends BaseCardTest {

    @Test
    void attackTriggerOnlyTargetsCreatureThatCrewedThisTurn() {
        Permanent schooner = addReadySchooner();
        Permanent crewer = addCreatureReady(player1, new IronpawAspirant());
        Permanent bystander = addCreatureReady(player1, new IronpawAspirant());

        crewSchooner(crewer);
        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(crewer.getId());
        assertThat(schooner.isTapped()).isTrue();
        assertThat(bystander.isTapped()).isFalse();
    }

    @Test
    void targetedCreatureExploresWhenAttackTriggerResolves() {
        addReadySchooner();
        Permanent crewer = addCreatureReady(player1, new IronpawAspirant());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        crewSchooner(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topLand);
    }

    @Test
    void summoningSickCreatureCanCrewAndExplore() {
        addReadySchooner();
        Permanent crewer = harness.addToBattlefieldAndReturn(player1, new IronpawAspirant());
        crewer.setSummoningSick(true);
        harness.setLibrary(player1, List.of());

        crewSchooner(crewer);
        assertThat(crewer.isTapped()).isTrue();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canTargetCreatureFromEitherCrewActivationButOnlyChosenCreatureExplores() {
        addReadySchooner();
        Permanent first = addCreatureReady(player1, new IronpawAspirant());
        Permanent second = addCreatureReady(player1, new IronpawAspirant());
        harness.setLibrary(player1, List.of());

        crewSchooner(first);
        crewSchooner(second);
        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void nonlandExploreCanPutRevealedCardIntoGraveyard() {
        assertNonlandExplore(true);
    }

    @Test
    void nonlandExploreCanLeaveRevealedCardOnTop() {
        assertNonlandExplore(false);
    }

    private void assertNonlandExplore(boolean putInGraveyard) {
        Permanent schooner = addReadySchooner();
        Permanent crewer = addCreatureReady(player1, new IronpawAspirant());
        IronpawAspirant revealed = new IronpawAspirant();
        harness.setLibrary(player1, List.of(revealed));

        crewSchooner(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(schooner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, putInGraveyard);
        if (putInGraveyard) {
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed);
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        } else {
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
            assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(revealed);
        }
    }

    @Test
    void emptyLibraryStillGivesCrewerCounter() {
        addReadySchooner();
        Permanent crewer = addCreatureReady(player1, new IronpawAspirant());
        harness.setLibrary(player1, List.of());

        crewSchooner(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void exploreUsesCreatureCurrentControllersLibrary() {
        addReadySchooner();
        Permanent crewer = addCreatureReady(player1, new IronpawAspirant());
        Forest ownLand = new Forest();
        Forest opponentsLand = new Forest();
        harness.setLibrary(player1, List.of(ownLand));
        harness.setLibrary(player2, List.of(opponentsLand));

        crewSchooner(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        gd.playerBattlefields.get(player1.getId()).remove(crewer);
        gd.playerBattlefields.get(player2.getId()).add(crewer);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(opponentsLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownLand);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ownLand);
    }

    @Test
    void attackTriggerStillResolvesAfterSchoonerLeavesBattlefield() {
        Permanent schooner = addReadySchooner();
        Permanent crewer = addCreatureReady(player1, new IronpawAspirant());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        crewSchooner(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        gd.playerBattlefields.get(player1.getId()).remove(schooner);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    void removedTargetDoesNotExplore() {
        addReadySchooner();
        Permanent crewer = addCreatureReady(player1, new IronpawAspirant());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        crewSchooner(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        gd.playerBattlefields.get(player1.getId()).remove(crewer);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
    }

    private Permanent addReadySchooner() {
        return addCreatureReady(player1, new SubterraneanSchooner());
    }

    private void crewSchooner(Permanent crewer) {
        harness.activateAbility(player1, 0, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        harness.passBothPriorities();
    }
}
