package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThelonsChantTest extends BaseCardTest {

    @Test
    @DisplayName("A Swamp's controller may put a -1/-1 counter on a creature")
    void swampControllerMayPutCounterOnCreature() {
        harness.addToBattlefield(player2, new ThelonsChant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Swamp()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Declining the counter choice deals 3 damage to the Swamp's controller")
    void swampControllerDeclinesAndTakesDamage() {
        harness.addToBattlefield(player2, new ThelonsChant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Swamp()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Without an eligible creature, the Swamp's controller takes 3 damage immediately")
    void swampControllerWithoutCreatureTakesDamage() {
        harness.addToBattlefield(player2, new ThelonsChant());

        harness.setHand(player1, List.of(new Swamp()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("With multiple creatures, the Swamp's controller chooses one for the counter")
    void swampControllerChoosesCreature() {
        harness.addToBattlefield(player2, new ThelonsChant());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Swamp()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Paying {G} during upkeep keeps Thelon's Chant")
    void payingUpkeepKeepsChant() {
        harness.addToBattlefield(player1, new ThelonsChant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Thelon's Chant");
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Thelon's Chant")
    void decliningUpkeepPaymentSacrificesChant() {
        harness.addToBattlefield(player1, new ThelonsChant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Thelon's Chant");
    }
}
