package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KindledFury.class, ElvishWarrior.class})
class KindledFuryTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Kindled Fury puts it on the stack targeting a creature")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new KindledFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Elvish Warrior");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Kindled Fury grants +1/+0 and first strike to target creature")
    void resolvingGrantsBoostAndFirstStrike() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new KindledFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Elvish Warrior");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent warrior = findPermanent(player1, "Elvish Warrior");
        assertThat(warrior.getPowerModifier()).isEqualTo(1);
        assertThat(warrior.getToughnessModifier()).isEqualTo(0);
        assertThat(warrior.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Kindled Fury can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new KindledFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Elvish Warrior");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent warrior = findPermanent(player2, "Elvish Warrior");
        assertThat(warrior.getPowerModifier()).isEqualTo(1);
        assertThat(warrior.getToughnessModifier()).isEqualTo(0);
        assertThat(warrior.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new KindledFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Elvish Warrior");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent warrior = findPermanent(player1, "Elvish Warrior");
        assertThat(warrior.getPowerModifier()).isEqualTo(0);
        assertThat(warrior.getToughnessModifier()).isEqualTo(0);
        assertThat(warrior.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Kindled Fury fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new KindledFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Elvish Warrior");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Kindled Fury");
    }

    @Test
    @DisplayName("Multiple Kindled Furies stack their boosts and expire together")
    void multipleCastsStackUntilEndOfTurn() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new KindledFury(), new KindledFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, warrior.getId());
        harness.castAndResolveInstant(player1, 0, warrior.getId());

        assertThat(warrior.getPowerModifier()).isEqualTo(2);
        assertThat(warrior.getToughnessModifier()).isZero();
        assertThat(warrior.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warrior.getPowerModifier()).isZero();
        assertThat(warrior.getToughnessModifier()).isZero();
        assertThat(warrior.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Kindled Fury affects only its targeted creature")
    void doesNotAffectOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new KindledFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        for (Permanent unaffected : List.of(other, opponent)) {
            assertThat(unaffected.getPowerModifier()).isZero();
            assertThat(unaffected.getToughnessModifier()).isZero();
            assertThat(unaffected.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        }
    }
}
