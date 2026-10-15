package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.k.KumenasSpeaker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({RiverHeraldsBoon.class, RaptorCompanion.class, KumenasSpeaker.class, MerrowCommerce.class})
class RiverHeraldsBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Puts +1/+1 counter on target creature and target Merfolk")
    void putsCounterOnCreatureAndMerfolk() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Raptor Companion");
        UUID merfolkId = harness.getPermanentId(player1, "Kumena's Speaker");
        harness.castAndResolveInstant(player1, 0, List.of(bearId, merfolkId));

        Permanent bear = findPermanent(player1, "Raptor Companion");
        Permanent merfolk = findPermanent(player1, "Kumena's Speaker");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts +1/+1 counter on creature only when no Merfolk target chosen")
    void putsCounterOnCreatureOnlyWithoutMerfolkTarget() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castAndResolveInstant(player1, 0, List.of(bearId));

        Permanent bear = findPermanent(player1, "Raptor Companion");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Merfolk targeted as both creature and Merfolk gets two +1/+1 counters")
    void merfolkTargetedAsBothGetsTwoCounters() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID merfolkId = harness.getPermanentId(player1, "Kumena's Speaker");
        harness.castAndResolveInstant(player1, 0, List.of(merfolkId, merfolkId));

        Permanent merfolk = findPermanent(player1, "Kumena's Speaker");
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-Merfolk creature as the Merfolk target")
    void cannotTargetNonMerfolkAsSecondTarget() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature1.getId(), creature2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Merfolk");
    }

    @Test
    @DisplayName("Can target opponent's creatures")
    void canTargetOpponentCreatures() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        harness.addToBattlefield(player2, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player2, "Raptor Companion");
        UUID merfolkId = harness.getPermanentId(player2, "Kumena's Speaker");
        harness.castAndResolveInstant(player1, 0, List.of(bearId, merfolkId));

        Permanent bear = findPermanent(player2, "Raptor Companion");
        Permanent merfolk = findPermanent(player2, "Kumena's Speaker");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Spell resolves on remaining target when second target is removed")
    void resolvesOnRemainingTargetWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Raptor Companion");
        UUID merfolkId = harness.getPermanentId(player1, "Kumena's Speaker");
        harness.castInstant(player1, 0, List.of(bearId, merfolkId));

        // Remove Merfolk before resolution
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(merfolkId));

        harness.passBothPriorities();

        // First target should still get its counter
        Permanent bear = findPermanent(player1, "Raptor Companion");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can put a counter on a noncreature Merfolk permanent")
    void canTargetNoncreatureMerfolk() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new MerrowCommerce());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), merfolk.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Merfolk gets only one counter when the optional target is omitted")
    void merfolkGetsOneCounterWithoutOptionalTarget() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(merfolk.getId()));

        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast without the mandatory creature target")
    void cannotCastWithoutCreatureTarget() {
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.<UUID>of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "River Heralds' Boon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The optional Merfolk still gets its counter when the first target is removed")
    void resolvesOnMerfolkWhenFirstTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, List.of(creature.getId(), merfolk.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "River Heralds' Boon");
    }

    @Test
    @DisplayName("No counters are placed when both targets have left the battlefield")
    void doesNotResolveWhenBothTargetsRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, List.of(creature.getId(), merfolk.getId()));

        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(creature, merfolk));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "River Heralds' Boon");
    }
}
