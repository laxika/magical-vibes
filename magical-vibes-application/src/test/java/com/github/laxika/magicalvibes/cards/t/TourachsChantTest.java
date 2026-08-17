package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TourachsChantTest extends BaseCardTest {

    @Test
    @DisplayName("A Forest's controller may put a -1/-1 counter on a creature")
    void forestControllerMayPutCounterOnCreature() {
        harness.addToBattlefield(player2, new TourachsChant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Declining the counter choice deals 3 damage to the Forest's controller")
    void forestControllerDeclinesAndTakesDamage() {
        harness.addToBattlefield(player2, new TourachsChant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Without an eligible creature, the Forest's controller takes 3 damage immediately")
    void forestControllerWithoutCreatureTakesDamage() {
        harness.addToBattlefield(player2, new TourachsChant());

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("With multiple creatures, the Forest's controller chooses one for the counter")
    void forestControllerChoosesCreature() {
        harness.addToBattlefield(player2, new TourachsChant());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Forest()));
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
    @DisplayName("Paying {B} during upkeep keeps Tourach's Chant")
    void payingUpkeepKeepsChant() {
        harness.addToBattlefield(player1, new TourachsChant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Tourach's Chant");
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Tourach's Chant")
    void decliningUpkeepPaymentSacrificesChant() {
        harness.addToBattlefield(player1, new TourachsChant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Tourach's Chant");
    }
}
