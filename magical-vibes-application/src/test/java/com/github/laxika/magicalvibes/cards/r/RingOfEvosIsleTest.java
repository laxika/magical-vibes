package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RingOfEvosIsle.class, FugitiveWizard.class, GrizzlyBears.class, Naturalize.class})
class RingOfEvosIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {1} attaches the Ring to target creature you control")
    void equipAttachesToCreature() {
        Permanent ring = addRingReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("{2} ability grants hexproof to the equipped creature until end of turn")
    void abilityGrantsHexproof() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Granted hexproof wears off at end of turn")
    void hexproofWearsOff() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("{2} ability does nothing while the Ring is unattached")
    void abilityDoesNothingWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addRingReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Upkeep trigger puts a +1/+1 counter on a blue equipped creature")
    void upkeepAddsCounterToBlueCreature() {
        Permanent creature = addCreatureReady(player1, new FugitiveWizard());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep trigger does nothing when the equipped creature is not blue")
    void upkeepDoesNothingForNonBlueCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger does nothing while the Ring is unattached")
    void upkeepDoesNothingWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new FugitiveWizard());
        addRingReady(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Hexproof ability still resolves after the Ring is destroyed in response")
    void hexproofResolvesAfterRingIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ring.getId());
        harness.assertInGraveyard(player1, "Ring of Evos Isle");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Upkeep counter ability still resolves after the Ring is destroyed in response")
    void upkeepResolvesAfterRingIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new FugitiveWizard());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Naturalize()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, ring.getId());
        harness.assertInGraveyard(player1, "Ring of Evos Isle");
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Ring does not add a counter during its opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent creature = addCreatureReady(player1, new FugitiveWizard());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Destroying the Ring does not remove hexproof already granted to its creature")
    void grantedHexproofSurvivesRingRemoval() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, ring.getId());

        harness.assertInGraveyard(player1, "Ring of Evos Isle");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    private Permanent addRingReady(Player player) {
        return addCreatureReady(player, new RingOfEvosIsle());
    }
}
