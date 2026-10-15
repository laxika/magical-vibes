package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Silkguard.class, GrizzlyBears.class, HolyStrength.class, LeoninScimitar.class, Unsummon.class})
class SilkguardTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on up to X target creatures you control")
    void putsCountersOnUpToXOwnCreatures() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Silkguard()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstantForX(player1, 0, 2, List.of(firstBear.getId()));
        harness.passBothPriorities();

        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Rejects creatures not controlled by the caster")
    void rejectsOpponentCreatureTarget() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Silkguard()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(opponentBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipment alone modifies a creature, but an opponent's Aura does not")
    void distinguishesEquipmentAndOpponentAuraModifications() {
        Permanent equippedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(equippedCreature.getId());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        opposingAura.setAttachedTo(enchantedCreature.getId());
        Permanent unattachedEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new Silkguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, unattachedEquipment, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingAura, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Hexproof recipients are fixed on resolution even if modifications change")
    void snapshotsModifiedCreaturesOnResolution() {
        Permanent modifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unmodifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Silkguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        unmodifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        lateCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, modifiedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodifiedCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("A positive X permits choosing no targets and still grants hexproof")
    void positiveXWithNoTargetsStillProtectsModifiedCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Silkguard()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Rejects more targets than the chosen X")
    void rejectsTooManyTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Silkguard()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No hexproof is granted when every chosen target becomes illegal")
    void allTargetsIllegalPreventsHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent modifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Silkguard(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, target.getId())).isNull();
        assertThat(gqs.hasKeyword(gd, modifiedCreature, Keyword.HEXPROOF)).isFalse();
        harness.assertInGraveyard(player1, "Silkguard");
    }

    @Test
    @DisplayName("One remaining legal target receives a counter and enables the hexproof effect")
    void partiallyIllegalTargetsStillResolve() {
        Permanent removedTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent remainingTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent modifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Silkguard(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantForX(player1, 0, 2, List.of(removedTarget.getId(), remainingTarget.getId()));
        harness.castAndResolveInstant(player1, 0, removedTarget.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, removedTarget.getId())).isNull();
        assertThat(remainingTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, remainingTarget, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, modifiedCreature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Gives hexproof to Auras, Equipment, and modified creatures until end of turn")
    void grantsHexproofToModifiedPermanents() {
        Permanent modifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unmodifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(modifiedCreature.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(modifiedCreature.getId());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Silkguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, modifiedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodifiedCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, modifiedCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.HEXPROOF)).isFalse();
    }
}
