package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PreyseizerDragon.class, GrizzlyBears.class, LlanowarElves.class})
class PreyseizerDragonTest extends BaseCardTest {

    private void castDragon() {
        harness.setHand(player1, new ArrayList<>(List.of(new PreyseizerDragon())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
    }

    private Permanent dragon() {
        return findPermanent(player1, "Preyseizer Dragon");
    }

    @Test
    @DisplayName("Devour 2 gives twice the number of sacrificed creatures in +1/+1 counters")
    void devourAddsTwiceTheSacrificedCreatures() {
        Permanent fodder1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodder2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDragon();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder1.getId(), fodder2.getId()));

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking deals damage equal to its +1/+1 counters to a creature")
    void attackingDealsCounterDamageToCreature() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castDragon();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(harness.getPermanentId(player1, "Grizzly Bears")));
        dragon().setSummoningSick(false);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
