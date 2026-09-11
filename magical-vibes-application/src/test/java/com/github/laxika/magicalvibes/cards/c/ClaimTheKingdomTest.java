package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClaimTheKingdom.class, Forest.class, GrizzlyBears.class})
class ClaimTheKingdomTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on a creature you control and a plan counter on Claim the Kingdom")
    void landfallPutsCountersOnTargetCreatureAndSource() {
        Permanent claim = addClaim();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(claim.getCounterCount(CounterType.PLAN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The fourth plan counter sacrifices Claim the Kingdom and gives a creature an indestructible counter")
    void fourthPlanCounterSacrificesAndPlacesIndestructibleCounter() {
        Permanent claim = addClaim();
        claim.setCounterCount(CounterType.PLAN, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(claim);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(claim.getCard());
        assertThat(creature.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall only offers creatures you control as targets")
    void landfallOnlyOffersControlledCreatures() {
        addClaim();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownCreature.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId());
    }

    private Permanent addClaim() {
        return harness.addToBattlefieldAndReturn(player1, new ClaimTheKingdom());
    }
}
