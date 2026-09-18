package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.h.HaplessResearcher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomNantuko.class, EmberShot.class, HaplessResearcher.class})
class PhantomNantukoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.castFromHand(player1, new PhantomNantuko(), "{2}{G}");
        harness.passBothPriorities();

        Permanent nantuko = findPermanent(player1, "Phantom Nantuko");
        assertThat(nantuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents damage and removes one +1/+1 counter")
    void preventsDamageAndRemovesOneCounter() {
        Permanent nantuko = addCreatureReady(player2, new PhantomNantuko());
        nantuko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castEmberShot(nantuko);

        assertThat(nantuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nantuko.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Still prevents damage when no +1/+1 counters remain")
    void preventsDamageWithoutCounters() {
        Permanent nantuko = addCreatureReady(player2, new PhantomNantuko());
        nantuko.setToughnessModifier(1);

        castEmberShot(nantuko);

        assertThat(nantuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nantuko.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nantuko);
    }

    @Test
    @DisplayName("Tap ability adds a +1/+1 counter")
    void tapAbilityAddsCounter() {
        Permanent nantuko = addCreatureReady(player1, new PhantomNantuko());
        nantuko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(nantuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(nantuko.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent nantuko = addCreatureReady(player1, new PhantomNantuko());
        nantuko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = addCreatureReady(player2, new HaplessResearcher());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(nantuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nantuko.getMarkedDamage()).isZero();
    }

    private void castEmberShot(Permanent target) {
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new PhantomNantuko()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
