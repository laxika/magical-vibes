package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChromaticLantern;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhoiraAgelessInnovator.class, FountainOfYouth.class, ChromaticLantern.class, GrizzlyBears.class})
class JhoiraAgelessInnovatorTest extends BaseCardTest {

    @Test
    void addsTwoIngenuityCountersBeforeOfferingAnArtifact() {
        Permanent jhoira = addJhoira();
        FountainOfYouth fountain = new FountainOfYouth();
        harness.setHand(player1, List.of(fountain, new ChromaticLantern(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jhoira.getCounterCount(CounterType.INGENUITY)).isEqualTo(2);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertInHand(player1, "Chromatic Lantern");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(jhoira.isTapped()).isTrue();
    }

    @Test
    void decliningLeavesEligibleArtifactInHand() {
        Permanent jhoira = addJhoira();
        FountainOfYouth fountain = new FountainOfYouth();
        harness.setHand(player1, List.of(fountain));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(jhoira.getCounterCount(CounterType.INGENUITY)).isEqualTo(2);
        harness.assertInHand(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    private Permanent addJhoira() {
        Permanent jhoira = new Permanent(new JhoiraAgelessInnovator());
        jhoira.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(jhoira);
        return jhoira;
    }
}
