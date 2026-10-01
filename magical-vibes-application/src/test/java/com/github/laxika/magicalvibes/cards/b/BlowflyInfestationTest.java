package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlowflyInfestation.class, SafeholdSentry.class, FlameJavelin.class})
class BlowflyInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("When a creature with a -1/-1 counter dies, puts a -1/-1 counter on target creature")
    void triggersWhenCreatureWithMinusOneCounterDies() {
        harness.addToBattlefield(player1, new BlowflyInfestation());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addToBattlefield(player2, new SafeholdSentry());

        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID dyingId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castAndResolveInstant(player1, 0, dyingId);
        // Flame Javelin resolves → Safehold Sentry dies with a -1/-1 counter → Blowfly triggers

        // Blowfly's controller (player1) chooses the target creature.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID targetId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities(); // Blowfly's ability resolves

        Permanent target = findPermanent(player2, "Safehold Sentry");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when the dying creature had no -1/-1 counter")
    void doesNotTriggerWhenNoMinusOneCounter() {
        harness.addToBattlefield(player1, new BlowflyInfestation());
        harness.addToBattlefield(player2, new SafeholdSentry());
        harness.addToBattlefield(player2, new SafeholdSentry());

        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID dyingId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castAndResolveInstant(player1, 0, dyingId);
        // Flame Javelin resolves → Safehold Sentry dies without a -1/-1 counter

        // Intervening-if fails, so nothing is queued and the other creature is untouched.
        assertThat(gd.interaction.activeInteraction()).isNull();
        Permanent target = findPermanents(player2, "Safehold Sentry").getFirst();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when the dying creature had a different kind of counter")
    void doesNotTriggerForDifferentCounterType() {
        harness.addToBattlefield(player1, new BlowflyInfestation());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        dying.setCounterCount(CounterType.STUN, 1);
        harness.addToBattlefield(player2, new SafeholdSentry());

        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID dyingId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castAndResolveInstant(player1, 0, dyingId);

        assertThat(gd.interaction.activeInteraction()).isNull();
        Permanent target = findPermanents(player2, "Safehold Sentry").getFirst();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can target a creature controlled by Blowfly Infestation's controller")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new BlowflyInfestation());
        harness.addToBattlefield(player1, new SafeholdSentry());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID dyingId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castAndResolveInstant(player1, 0, dyingId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID targetId = harness.getPermanentId(player1, "Safehold Sentry");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Safehold Sentry");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prompt for a target when no other creature is on the battlefield")
    void noTargetWhenNoOtherCreature() {
        harness.addToBattlefield(player1, new BlowflyInfestation());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID dyingId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.castAndResolveInstant(player1, 0, dyingId);
        // Safehold Sentry is the only creature — the trigger has no legal target

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
