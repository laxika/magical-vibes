package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerialSurveyor.class, Forest.class, GrizzlyBears.class, Plains.class})
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

    private Permanent addReadySurveyor() {
        Permanent surveyor = harness.addToBattlefieldAndReturn(player1, new AerialSurveyor());
        surveyor.setSummoningSick(false);
        return surveyor;
    }

    private void crew(Permanent surveyor) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(surveyor), null, null);
        harness.passBothPriorities();
    }
}
