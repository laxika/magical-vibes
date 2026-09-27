package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoutewayMoose.class, Forest.class, GrizzlyBears.class})
class RoutewayMooseTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled seeks a land onto the battlefield tapped")
    void attacksWhileSaddledSeeksTappedLand() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent moose = addCreatureReady(player1, new RoutewayMoose());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(moose.isSaddled()).isTrue();
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
}
