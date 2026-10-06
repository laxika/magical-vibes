package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoltCharge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SchemingAspirant.class, GrizzlyBears.class, VoltCharge.class})
class SchemingAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 2 life and controller gains 2 life after proliferating")
    void drainsOpponentsAfterProliferating() {
        harness.addToBattlefield(player1, new SchemingAspirant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers even when no permanent or player is chosen for proliferate")
    void triggersWhenNothingIsChosenForProliferate() {
        harness.addToBattlefield(player1, new SchemingAspirant());

        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Opponent proliferating does not trigger your Aspirant")
    void doesNotTriggerForOpponentProliferating() {
        harness.addToBattlefield(player1, new SchemingAspirant());
        harness.setHand(player2, List.of(new VoltCharge()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Aspirant triggers independently for one proliferate event")
    void eachAspirantTriggersIndependently() {
        harness.addToBattlefield(player1, new SchemingAspirant());
        harness.addToBattlefield(player1, new SchemingAspirant());
        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Choosing no eligible permanents still triggers the ability")
    void triggersWhenEligiblePermanentIsDeclined() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new SchemingAspirant());
        aspirant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 15);
        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Aspirant still drains after dying during the proliferating spell")
    void triggerResolvesAfterSourceDies() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new SchemingAspirant());
        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, aspirant.getId());
        harness.assertNotOnBattlefield(player1, "Scheming Aspirant");
        harness.assertInGraveyard(player1, "Scheming Aspirant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
