package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PutridGoblin.class, Eviscerate.class})
class PutridGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Persist returns Putrid Goblin with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new PutridGoblin());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Putrid Goblin"));
        resolveAllTriggers();

        Permanent goblin = findPermanent(player1, "Putrid Goblin");
        assertThat(goblin.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(goblin.getEffectivePower()).isEqualTo(1);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist does not return Putrid Goblin when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new PutridGoblin());
        goblin.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, goblin.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Putrid Goblin");
        harness.assertInGraveyard(player1, "Putrid Goblin");
    }

    @Test
    @DisplayName("Persist waits on the stack before returning Putrid Goblin")
    void persistUsesTheStack() {
        harness.addToBattlefield(player1, new PutridGoblin());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0,
                harness.getPermanentId(player1, "Putrid Goblin"));

        harness.assertNotOnBattlefield(player1, "Putrid Goblin");
        harness.assertInGraveyard(player1, "Putrid Goblin");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanent(player1, "Putrid Goblin")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Persist returns a stolen Putrid Goblin under its owner's control")
    void persistReturnsUnderOwnersControl() {
        PutridGoblin card = new PutridGoblin();
        card.setOwnerId(player2.getId());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(goblin.getId(), player2.getId());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, goblin.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Putrid Goblin");
        Permanent returned = findPermanent(player2, "Putrid Goblin");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(returned.getId()).isNotEqualTo(goblin.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Putrid Goblin stays dead when killed again after persisting")
    void secondDeathDoesNotPersistAgain() {
        harness.addToBattlefield(player1, new PutridGoblin());
        harness.setHand(player1, List.of(new Eviscerate(), new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Putrid Goblin"));
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Putrid Goblin")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Putrid Goblin"));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Putrid Goblin");
        harness.assertInGraveyard(player1, "Putrid Goblin");
    }

    @Test
    @DisplayName("Plus counters do not prevent persist and are lost when Putrid Goblin returns")
    void persistIgnoresAndLosesPlusCounters() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new PutridGoblin());
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, goblin.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Putrid Goblin");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(returned.getId()).isNotEqualTo(goblin.getId());
    }
}
