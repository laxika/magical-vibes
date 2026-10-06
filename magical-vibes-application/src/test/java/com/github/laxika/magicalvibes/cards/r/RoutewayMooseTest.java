package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoutewayMoose.class, Forest.class})
class RoutewayMooseTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled seeks a land onto the battlefield tapped")
    void attacksWhileSaddledSeeksTappedLand() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent moose = addCreatureReady(player1, new RoutewayMoose());
        Permanent rider = addCreatureReady(player1, new RoutewayMoose());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(moose.isSaddled()).isTrue();
        assertThat(rider.isTapped()).isTrue();
        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking while not saddled does not seek a land")
    void doesNotTriggerWhenNotSaddled() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addCreatureReady(player1, new RoutewayMoose());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("A saddled attack seeks exactly one land and preserves the other library cards")
    void seeksExactlyOneLandFromMixedLibrary() {
        RoutewayMoose firstNonland = new RoutewayMoose();
        RoutewayMoose secondNonland = new RoutewayMoose();
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstNonland, firstLand, secondNonland, secondLand));
        addCreatureReady(player1, new RoutewayMoose());
        addCreatureReady(player1, new RoutewayMoose());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).containsSubsequence(firstNonland, secondNonland);
        assertThat(gd.playerDecks.get(player1.getId())).containsAnyOf(firstLand, secondLand);
    }

    @Test
    @DisplayName("A saddled attack with no land in the library leaves the library unchanged")
    void noMatchingLandDoesNothing() {
        RoutewayMoose nonland = new RoutewayMoose();
        harness.setLibrary(player1, List.of(nonland));
        addCreatureReady(player1, new RoutewayMoose());
        addCreatureReady(player1, new RoutewayMoose());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(countPermanents(player1, "Routeway Moose")).isEqualTo(2);
    }

    @Test
    @DisplayName("The Moose cannot saddle itself")
    void cannotPaySaddleWithItself() {
        Permanent moose = addCreatureReady(player1, new RoutewayMoose());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(moose.isTapped()).isFalse();
        assertThat(moose.isSaddled()).isFalse();
    }
}
