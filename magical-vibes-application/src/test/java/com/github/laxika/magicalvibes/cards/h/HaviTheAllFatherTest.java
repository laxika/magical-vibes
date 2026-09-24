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

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(eligible.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
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
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(eligible.getId()))
                .findFirst()
                .orElseThrow()
                .isTapped()).isTrue();
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

    private void destroyWithDestruction(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, permanent));
        harness.passBothPriorities();
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
