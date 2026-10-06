package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RingOfValkas.class, CanyonMinotaur.class, WalkingCorpse.class})
class RingOfValkasTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {1} attaches the Ring to target creature you control")
    void equipAttachesToCreature() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfValkas());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature has haste")
    void equippedCreatureHasHaste() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfValkas());
        ring.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures do not gain haste")
    void unattachedRingGrantsNothing() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addToBattlefieldAndReturn(player1, new RingOfValkas());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Upkeep trigger puts a +1/+1 counter on a red equipped creature")
    void upkeepAddsCounterToRedCreature() {
        Permanent creature = addCreatureReady(player1, new CanyonMinotaur());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfValkas());
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep trigger does nothing when the equipped creature is not red")
    void upkeepDoesNothingForNonRedCreature() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfValkas());
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger does nothing while the Ring is unattached")
    void upkeepDoesNothingWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new CanyonMinotaur());
        harness.addToBattlefieldAndReturn(player1, new RingOfValkas());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The Ring does not trigger during its opponent's upkeep")
    void opponentUpkeepDoesNotAddCounter() {
        Permanent creature = addCreatureReady(player1, new CanyonMinotaur());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfValkas());
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Reequipping transfers haste without removing existing counters")
    void reequippingTransfersHasteAndKeepsCounters() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfValkas());
        Permanent original = addCreatureReady(player1, new CanyonMinotaur());
        Permanent replacement = addCreatureReady(player1, new WalkingCorpse());
        ring.setAttachedTo(original.getId());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.HASTE)).isTrue();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The upkeep ability uses the red creature equipped at resolution")
    void upkeepUsesCurrentAttachment() {
        Permanent original = addCreatureReady(player1, new WalkingCorpse());
        Permanent replacement = addCreatureReady(player1, new CanyonMinotaur());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfValkas());
        ring.setAttachedTo(original.getId());

        advanceToUpkeep(player1);
        ring.setAttachedTo(replacement.getId());
        harness.passBothPriorities();

        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep ability checks the equipped creature's color at resolution")
    void upkeepDoesNotUsePreviouslyEquippedRedCreature() {
        Permanent original = addCreatureReady(player1, new CanyonMinotaur());
        Permanent replacement = addCreatureReady(player1, new WalkingCorpse());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfValkas());
        ring.setAttachedTo(original.getId());

        advanceToUpkeep(player1);
        ring.setAttachedTo(replacement.getId());
        harness.passBothPriorities();

        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Detaching the Ring before its upkeep ability resolves adds no counter")
    void detachedRingAddsNoCounter() {
        Permanent creature = addCreatureReady(player1, new CanyonMinotaur());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfValkas());
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        ring.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }
}
