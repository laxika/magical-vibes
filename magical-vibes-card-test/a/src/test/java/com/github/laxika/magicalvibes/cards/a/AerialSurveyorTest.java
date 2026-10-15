package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrairieStream;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerialSurveyor.class, Forest.class, GrizzlyBears.class, Plains.class, PrairieStream.class})
class AerialSurveyorTest extends BaseCardTest {

    @Test
    void attackingDefendingPlayerWithMoreLandsSearchesForTappedPlains() {
        Permanent surveyor = addReadySurveyor();
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains()));

        crew(surveyor);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Plains")).singleElement()
                .satisfies(plains -> assertThat(plains.isTapped()).isTrue());
    }

    @Test
    void doesNotSearchWhenDefendingPlayerDoesNotHaveMoreLands() {
        Permanent surveyor = addReadySurveyor();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Plains()));

        crew(surveyor);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Plains")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card instanceof Plains);
    }

    @Test
    void conditionIsRecheckedWhenTriggerResolves() {
        Permanent surveyor = addReadySurveyor();
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains()));
        crew(surveyor);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new Plains());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Plains")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(Plains.class);
    }

    @Test
    void gainingALandAdvantageAfterAttackingDoesNotCreateATrigger() {
        Permanent surveyor = addReadySurveyor();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Plains()));
        crew(surveyor);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player2, new Forest());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Plains")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(Plains.class);
    }

    @Test
    void basicLandWithoutPlainsSubtypeCannotBeFound() {
        Permanent surveyor = addReadySurveyor();
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        crew(surveyor);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
    }

    @Test
    void canFailToFindEvenWithAPlainsInLibrary() {
        Permanent surveyor = addReadySurveyor();
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains()));
        crew(surveyor);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Plains")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(Plains.class);
    }

    @Test
    void nonbasicPlainsCannotBeFound() {
        Permanent surveyor = addReadySurveyor();
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new PrairieStream()));
        crew(surveyor);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Prairie Stream")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(PrairieStream.class);
    }

    @Test
    void removingSurveyorDoesNotStopItsSearchTrigger() {
        Permanent surveyor = addReadySurveyor();
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains()));
        crew(surveyor);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(surveyor);
        gd.playerGraveyards.get(player1.getId()).add(surveyor.getCard());
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Plains")).singleElement()
                .satisfies(plains -> assertThat(plains.isTapped()).isTrue());
    }

    @Test
    void summoningSickCreatureCanCrewAndCrewDoesNotTapVehicle() {
        Permanent surveyor = addReadySurveyor();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        crew(surveyor);

        assertThat(creature.isTapped()).isTrue();
        assertThat(surveyor.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, surveyor)).isTrue();
    }

    private Permanent addReadySurveyor() {
        return addCreatureReady(player1, new AerialSurveyor());
    }

    private void crew(Permanent surveyor) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(surveyor), null, null);
        harness.passBothPriorities();
    }
}
