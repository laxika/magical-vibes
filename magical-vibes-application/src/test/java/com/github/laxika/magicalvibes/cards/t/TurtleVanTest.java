package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DonatelloTurtleTechie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurtleVan.class, GrizzlyBears.class, DonatelloTurtleTechie.class})
class TurtleVanTest extends BaseCardTest {

    @Test
    void attackTriggerOnlyTargetsCreatureThatCrewedThisTurn() {
        addReadyVan();
        Permanent crewer = addCreatureReady(player1, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());

        crewVan(crewer);
        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(crewer.getId());
        assertThat(bystander.isTapped()).isFalse();
    }

    @Test
    void nonPartyCreatureGetsOnePlusOneCounter() {
        addReadyVan();
        Permanent crewer = addCreatureReady(player1, new GrizzlyBears());

        crewVan(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void partyCreatureGetsItsPlusOneCountersDoubled() {
        addReadyVan();
        Permanent crewer = addCreatureReady(player1, new DonatelloTurtleTechie());
        crewer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        crewVan(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    private Permanent addReadyVan() {
        Permanent van = harness.addToBattlefieldAndReturn(player1, new TurtleVan());
        van.setSummoningSick(false);
        return van;
    }

    private void crewVan(Permanent crewer) {
        harness.activateAbility(player1, 0, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        harness.passBothPriorities();
    }
}
