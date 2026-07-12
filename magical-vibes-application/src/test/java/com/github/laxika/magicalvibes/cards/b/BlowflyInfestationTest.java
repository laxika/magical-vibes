package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlowflyInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("When a creature with a -1/-1 counter dies, puts a -1/-1 counter on target creature")
    void triggersWhenCreatureWithMinusOneCounterDies() {
        harness.addToBattlefield(player1, new BlowflyInfestation());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addToBattlefield(player2, new HillGiant());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, dyingId);
        harness.passBothPriorities(); // Shock resolves → Grizzly Bears dies with a -1/-1 counter → Blowfly triggers

        // Blowfly's controller (player1) chooses the target creature.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities(); // Blowfly's ability resolves

        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElseThrow();
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when the dying creature had no -1/-1 counter")
    void doesNotTriggerWhenNoMinusOneCounter() {
        harness.addToBattlefield(player1, new BlowflyInfestation());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, dyingId);
        harness.passBothPriorities(); // Shock resolves → Grizzly Bears dies without a -1/-1 counter

        // Intervening-if fails, so nothing is queued and the other creature is untouched.
        assertThat(gd.interaction.activeInteraction()).isNull();
        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElseThrow();
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not prompt for a target when no other creature is on the battlefield")
    void noTargetWhenNoOtherCreature() {
        harness.addToBattlefield(player1, new BlowflyInfestation());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, dyingId);
        harness.passBothPriorities(); // Grizzly Bears is the only creature — the trigger has no legal target

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
