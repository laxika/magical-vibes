package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantomCentaur.class, CabalTrainee.class, FlaringPain.class,
        GiantWarthog.class, LavaDart.class})
class PhantomCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.castFromHand(player1, new PhantomCentaur(), "{2}{G}{G}");
        harness.passBothPriorities();

        Permanent centaur = findPermanent(player1, "Phantom Centaur");
        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage is prevented and removes one +1/+1 counter")
    void damageIsPreventedAndRemovesOneCounter() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player2, new PhantomCentaur());
        centaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        dealLavaDart(centaur);

        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(centaur.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage is still prevented when no +1/+1 counters remain")
    void preventsDamageWithoutCounters() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player2, new PhantomCentaur());
        centaur.setToughnessModifier(1);

        dealLavaDart(centaur);

        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(centaur.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(centaur);
    }

    @Test
    @DisplayName("Unpreventable damage still removes one +1/+1 counter")
    void unpreventableDamageStillRemovesOneCounter() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player2, new PhantomCentaur());
        centaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        centaur.setToughnessModifier(3);

        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();
        dealLavaDart(centaur);

        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(centaur.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(centaur);
    }

    @Test
    @DisplayName("Simultaneous combat damage from multiple sources removes only one counter")
    void simultaneousCombatDamageFromMultipleSourcesRemovesOnlyOneCounter() {
        Permanent centaur = addCreatureReady(player1, new PhantomCentaur());
        centaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        centaur.setToughnessModifier(10);
        Permanent firstBlocker = addCreatureReady(player2, new GiantWarthog());
        Permanent secondBlocker = addCreatureReady(player2, new GiantWarthog());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 2));

        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(centaur.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Protection from black prevents black abilities from targeting it")
    void protectionFromBlackPreventsTargeting() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player2, new PhantomCentaur());
        addCreatureReady(player1, new CabalTrainee());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, centaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    private void dealLavaDart(Permanent target) {
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
