package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({VampiresZeal.class, AncientBrontodon.class, QueensBaySoldier.class})
class VampiresZealTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving on non-Vampire creature gives +2/+2 but NOT first strike")
    void nonVampireGetsBoostButNotFirstStrike() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AncientBrontodon());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(11);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Resolving on Vampire creature gives +2/+2 AND first strike")
    void vampireGetsBoostAndFirstStrike() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, vampire.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, vampire)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, vampire)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, vampire.getId());

        harness.forceStep(TurnStep.END_STEP);
        assertThat(PermanentGrants.powerBoostOf(gd, vampire)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, vampire)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.FIRST_STRIKE)).isTrue();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, vampire)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, vampire)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AncientBrontodon());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, creature.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Vampire's Zeal");
    }

    @Test
    @DisplayName("Can target opponent's Vampire creature and grant first strike")
    void canTargetOpponentVampire() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, vampire.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, vampire)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, vampire)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Multiple copies stack their boosts and all expire at cleanup")
    void multipleCopiesStackAndExpire() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new VampiresZeal(), new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, vampire.getId());
        harness.castAndResolveInstant(player1, 0, vampire.getId());

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, vampire)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, vampire)).isZero();
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's non-Vampire gets the boost without first strike")
    void canTargetOpponentNonVampire() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }
}
