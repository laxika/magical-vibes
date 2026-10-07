package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ParadiseDruid;
import com.github.laxika.magicalvibes.cards.p.Prismite;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeWipe.class, GiantSpider.class, GrizzlyBears.class, HillGiant.class, ParadiseDruid.class, Prismite.class})
class TimeWipeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a chosen creature you control before destroying all remaining creatures")
    void returnsChosenCreatureThenDestroysAllOtherCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GiantSpider());

        castTimeWipe();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(bearsId,
                harness.getPermanentId(player1, "Hill Giant"));
        assertThat(choice.validPermanentIds()).doesNotContain(harness.getPermanentId(player2, "Giant Spider"));

        harness.handlePermanentChosen(player1, bearsId);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Destroys all creatures when you control none to return")
    void destroysAllCreaturesWhenNoCreatureCanBeReturned() {
        addCreatureReady(player2, new GiantSpider());

        castTimeWipe();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Returns a creature to its owner rather than its controller")
    void returnsBorrowedCreatureToOpponentsHand() {
        ParadiseDruid druid = new ParadiseDruid();
        druid.setOwnerId(player2.getId());
        UUID druidId = addCreatureReady(player1, druid).getId();
        addCreatureReady(player2, new Prismite());

        castTimeWipe();
        harness.handlePermanentChosen(player1, druidId);

        harness.assertInHand(player2, "Paradise Druid");
        harness.assertNotInHand(player1, "Paradise Druid");
        harness.assertNotOnBattlefield(player1, "Paradise Druid");
        harness.assertInGraveyard(player2, "Prismite");
    }

    @Test
    @DisplayName("Destroys an opposing hexproof creature and an artifact creature")
    void destroysHexproofAndArtifactCreaturesWithoutTargeting() {
        addCreatureReady(player2, new ParadiseDruid());
        addCreatureReady(player2, new Prismite());

        castTimeWipe();

        harness.assertInGraveyard(player2, "Paradise Druid");
        harness.assertInGraveyard(player2, "Prismite");
        harness.assertNotOnBattlefield(player2, "Paradise Druid");
        harness.assertNotOnBattlefield(player2, "Prismite");
    }

    @Test
    @DisplayName("Must return the only creature you control even when no others remain")
    void returnsOnlyCreatureBeforeFinishingResolution() {
        UUID prismiteId = addCreatureReady(player1, new Prismite()).getId();

        castTimeWipe();
        harness.handlePermanentChosen(player1, prismiteId);

        harness.assertInHand(player1, "Prismite");
        harness.assertNotInGraveyard(player1, "Prismite");
        harness.assertNotOnBattlefield(player1, "Prismite");
        harness.assertInGraveyard(player1, "Time Wipe");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castTimeWipe() {
        harness.castFromHand(player1, new TimeWipe(), "{2}{W}{W}{U}");
        harness.passBothPriorities();
    }
}
