package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdventurersAirship.class, Forest.class, GrizzlyBears.class})
class AdventurersAirshipTest extends BaseCardTest {

    @Test
    void crewAnimatesTheVehicleAndTapsTheCrew() {
        Permanent airship = addReadyAirship(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, airship)).isTrue();
        assertThat(bear.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, airship)).isFalse();
    }

    @Test
    void attackingDrawsThenDiscards() {
        addReadyAirship(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Card cardToDiscard = new GrizzlyBears();
        Card cardToDraw = new Forest();
        harness.setHand(player1, List.of(cardToDiscard));
        harness.setLibrary(player1, List.of(cardToDraw));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cardToDiscard);
        assertThat(gd.playerHands.get(player1.getId())).contains(cardToDraw);
    }

    private Permanent addReadyAirship(Player player) {
        Permanent airship = new Permanent(new AdventurersAirship());
        airship.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(airship);
        return airship;
    }
}
