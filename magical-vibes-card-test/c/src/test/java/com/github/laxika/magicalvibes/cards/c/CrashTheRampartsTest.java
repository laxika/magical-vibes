package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.b.BlossomDryad;
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

@CardUsed({CrashTheRamparts.class, BlossomDryad.class})
class CrashTheRampartsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Crash the Ramparts puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new BlossomDryad());
        harness.setHand(player1, List.of(new CrashTheRamparts()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Blossom Dryad");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Crash the Ramparts");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Crash the Ramparts gives +3/+3 and trample to target creature")
    void resolvingBoostsAndGrantsTrample() {
        harness.addToBattlefield(player1, new BlossomDryad());
        harness.setHand(player1, List.of(new CrashTheRamparts()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Blossom Dryad");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BlossomDryad());
        harness.setHand(player1, List.of(new CrashTheRamparts()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Blossom Dryad");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Crash the Ramparts fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new BlossomDryad());
        harness.setHand(player1, List.of(new CrashTheRamparts()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Blossom Dryad");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Crash the Ramparts");
    }

    @Test
    @DisplayName("Crash the Ramparts can boost an opponent's creature without affecting others")
    void canTargetOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BlossomDryad());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlossomDryad());
        harness.setHand(player1, List.of(new CrashTheRamparts()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, ownCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownCreature)).isZero();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Crash the Ramparts");
    }

    @Test
    @DisplayName("Repeated casts stack their boosts and all expire at end of turn")
    void repeatedCastsStackAndExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BlossomDryad());
        harness.setHand(player1, List.of(new CrashTheRamparts(), new CrashTheRamparts()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }
}
