package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaviTheAllFather.class, IsamaruHoundOfKonda.class, Millstone.class,
        GrizzlyBears.class})
class HaviTheAllFatherTest extends BaseCardTest {

    @Test
    @DisplayName("Havi has indestructible with four historic cards in its controller's graveyard")
    void hasIndestructibleWithFourHistoricCards() {
        harness.setGraveyard(player1, List.of(
                new Millstone(), new Millstone(), new Millstone(), new Millstone()));
        Permanent havi = harness.addToBattlefieldAndReturn(player1, new HaviTheAllFather());
        destroyWithDestruction(havi);

        harness.assertOnBattlefield(player1, "Havi, the All-Father");
        harness.assertNotInGraveyard(player1, "Havi, the All-Father");
    }

    @Test
    @DisplayName("Havi is destructible with fewer than four historic cards")
    void isDestructibleWithFewerThanFourHistoricCards() {
        harness.setGraveyard(player1, List.of(new Millstone(), new Millstone(), new Millstone()));
        Permanent havi = harness.addToBattlefieldAndReturn(player1, new HaviTheAllFather());
        destroyWithDestruction(havi);

        harness.assertNotOnBattlefield(player1, "Havi, the All-Father");
        harness.assertInGraveyard(player1, "Havi, the All-Father");
    }

    @Test
    @DisplayName("Havi returns a lesser legendary creature when it dies")
    void returnsLesserLegendaryCreatureWhenItDies() {
        Card eligible = new IsamaruHoundOfKonda();
        Card equalManaValue = new HaviTheAllFather();
        harness.setGraveyard(player1, List.of(eligible, equalManaValue));
        Permanent havi = harness.addToBattlefieldAndReturn(player1, new HaviTheAllFather());

        destroy(havi);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Isamaru, Hound of Konda").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Havi, the All-Father");
    }

    @Test
    @DisplayName("Havi triggers when another legendary creature you control dies")
    void triggersWhenAnotherLegendaryCreatureDies() {
        Card eligible = new IsamaruHoundOfKonda();
        harness.setGraveyard(player1, List.of(eligible));
        harness.addToBattlefield(player1, new HaviTheAllFather());
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent dyingLegendary = harness.addToBattlefieldAndReturn(player1, legendaryBears);

        destroy(dyingLegendary);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(findPermanent(player1, "Isamaru, Hound of Konda").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Havi does not trigger for a nonlegendary creature")
    void doesNotTriggerForNonlegendaryCreature() {
        Card eligible = new IsamaruHoundOfKonda();
        harness.setGraveyard(player1, List.of(eligible));
        harness.addToBattlefield(player1, new HaviTheAllFather());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroy(dyingCreature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Isamaru, Hound of Konda");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void legendaryCardsCountAsHistoricAndProtectionUpdatesWhenTheyLeave() {
        harness.setGraveyard(player1, List.of(new IsamaruHoundOfKonda(),
                new IsamaruHoundOfKonda(), new IsamaruHoundOfKonda(), new IsamaruHoundOfKonda()));
        Permanent havi = harness.addToBattlefieldAndReturn(player1, new HaviTheAllFather());

        destroyWithDestruction(havi);
        harness.assertOnBattlefield(player1, "Havi, the All-Father");

        harness.setGraveyard(player1, List.of(new Millstone(), new Millstone(), new Millstone()));
        destroyWithDestruction(havi);
        harness.assertInGraveyard(player1, "Havi, the All-Father");
        harness.assertNotOnBattlefield(player1, "Havi, the All-Father");
    }

    @Test
    void nonhistoricCardsDoNotMeetTheThreshold() {
        harness.setGraveyard(player1, List.of(new Millstone(), new Millstone(),
                new Millstone(), new GrizzlyBears()));
        Permanent havi = harness.addToBattlefieldAndReturn(player1, new HaviTheAllFather());

        destroyWithDestruction(havi);

        harness.assertInGraveyard(player1, "Havi, the All-Father");
        harness.assertNotOnBattlefield(player1, "Havi, the All-Father");
    }

    @Test
    void opponentsHistoricCardsDoNotGrantIndestructible() {
        harness.setGraveyard(player2, List.of(new Millstone(), new Millstone(),
                new Millstone(), new Millstone()));
        Permanent havi = harness.addToBattlefieldAndReturn(player1, new HaviTheAllFather());

        destroyWithDestruction(havi);

        harness.assertInGraveyard(player1, "Havi, the All-Father");
        harness.assertNotOnBattlefield(player1, "Havi, the All-Father");
    }

    @Test
    void deathTriggerTargetsOnlyLegendaryCreaturesInYourGraveyard() {
        Card eligible = new IsamaruHoundOfKonda();
        Card nonlegendary = new GrizzlyBears();
        Card artifact = new Millstone();
        Card opposingLegendary = new IsamaruHoundOfKonda();
        harness.setGraveyard(player1, List.of(eligible, nonlegendary, artifact));
        harness.setGraveyard(player2, List.of(opposingLegendary));
        Permanent havi = harness.addToBattlefieldAndReturn(player1, new HaviTheAllFather());

        destroy(havi);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Isamaru, Hound of Konda").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
    }

    @Test
    void anotherLegendarysDeathUsesItsManaValueRatherThanHavis() {
        harness.setGraveyard(player1, List.of(new IsamaruHoundOfKonda()));
        harness.addToBattlefield(player1, new HaviTheAllFather());
        Permanent dyingLegendary = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());

        destroy(dyingLegendary);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opposingLegendarysDeathDoesNotTriggerHavi() {
        harness.setGraveyard(player1, List.of(new IsamaruHoundOfKonda()));
        harness.addToBattlefield(player1, new HaviTheAllFather());
        Permanent opposingLegendary = harness.addToBattlefieldAndReturn(player2, new HaviTheAllFather());

        destroy(opposingLegendary);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Isamaru, Hound of Konda");
        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetLeavingTheGraveyardBeforeResolutionIsNotReturned() {
        Card eligible = new IsamaruHoundOfKonda();
        harness.setGraveyard(player1, List.of(eligible));
        Permanent havi = harness.addToBattlefieldAndReturn(player1, new HaviTheAllFather());
        destroy(havi);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardById(gd, eligible.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(gd.stack).isEmpty();
    }

    private void destroyWithDestruction(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, permanent));
        harness.passBothPriorities();
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
