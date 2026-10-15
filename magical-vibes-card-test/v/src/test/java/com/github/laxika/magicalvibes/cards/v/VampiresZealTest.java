package com.github.laxika.magicalvibes.cards.v;

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

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.getEffectivePower()).isEqualTo(11);
        assertThat(creature.getEffectiveToughness()).isEqualTo(11);
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Resolving on Vampire creature gives +2/+2 AND first strike")
    void vampireGetsBoostAndFirstStrike() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, vampire.getId());

        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(2);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, vampire.getId());

        harness.forceStep(TurnStep.END_STEP);
        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(2);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isEqualTo(0);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
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

        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(2);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
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

        assertThat(vampire.getEffectivePower()).isEqualTo(6);
        assertThat(vampire.getEffectiveToughness()).isEqualTo(6);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isZero();
        assertThat(vampire.getToughnessModifier()).isZero();
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's non-Vampire gets the boost without first strike")
    void canTargetOpponentNonVampire() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }
}
