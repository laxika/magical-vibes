package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokkaLateralStrategist.class, GrizzlyBears.class, Forest.class})
class SokkaLateralStrategistTest extends BaseCardTest {

    @Test
    void attackingWithAnotherCreatureDrawsACard() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        addReadySokka();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void attackingAloneDoesNotDraw() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        addReadySokka();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void attackingWithoutSokkaDoesNotTriggerItsAbility() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        addReadySokka();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    private Permanent addReadySokka() {
        return addCreatureReady(player1, new SokkaLateralStrategist());
    }
}
