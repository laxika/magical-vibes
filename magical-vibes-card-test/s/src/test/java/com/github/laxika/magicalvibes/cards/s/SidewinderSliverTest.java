package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidewinderSliver.class, BonesplitterSliver.class, AshcoatBear.class, SuddenDeath.class})
class SidewinderSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Sidewinder Sliver grants itself flanking")
    void grantsSelfFlanking() {
        Permanent sliver = addCreatureReady(player1, new SidewinderSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Grants flanking to another Sliver you control")
    void grantsFlankingToOtherSliver() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonesplitterSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Grants flanking to an opponent's Sliver too")
    void grantsFlankingToOpponentSliver() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonesplitterSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Does not grant flanking to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Granted flanking gives a non-flanking blocker -1/-1")
    void grantedFlankingShrinksNonFlankingBlocker() {
        Permanent attacker = addCreatureReady(player1, new SidewinderSliver());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Sidewinder Slivers give separate flanking triggers")
    void multipleSourcesGiveSeparateFlankingTriggers() {
        addCreatureReady(player1, new SidewinderSliver());
        addCreatureReady(player2, new SidewinderSliver());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
            assertThat(gd.stack).hasSize(2);
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player2, "Ashcoat Bear");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("A Sliver blocker with granted flanking does not trigger flanking")
    void sliverBlockerWithFlankingIsNotShrunk() {
        addCreatureReady(player1, new SidewinderSliver());
        Permanent blocker = addCreatureReady(player2, new BonesplitterSliver());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

            assertThat(gd.stack).isEmpty();
            assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("A non-Sliver attacker does not get a flanking trigger")
    void nonSliverAttackerDoesNotTriggerFlanking() {
        addCreatureReady(player1, new SidewinderSliver());
        addCreatureReady(player1, new AshcoatBear());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

            assertThat(gd.stack).isEmpty();
            assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Flanking grant ends for both players when Sidewinder Sliver dies")
    void grantEndsWhenSourceDies() {
        Permanent source = addCreatureReady(player1, new SidewinderSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonesplitterSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonesplitterSliver());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.FLANKING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.FLANKING)).isTrue();

        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.assertInGraveyard(player1, "Sidewinder Sliver");
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.FLANKING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Flanking trigger resolves after the granting source dies")
    void pendingFlankingSurvivesSourceRemoval() {
        Permanent source = addCreatureReady(player1, new SidewinderSliver());
        Permanent attacker = addCreatureReady(player1, new BonesplitterSliver());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackersAndPrepareBlockers(List.of(1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
            assertThat(gd.stack).hasSize(1);

            harness.castAndResolveInstant(player1, 0, source.getId());

            harness.assertInGraveyard(player1, "Sidewinder Sliver");
            assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLANKING)).isFalse();
            resolveAllTriggers();

            assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
        });
    }
}
