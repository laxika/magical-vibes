package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.k.KitsuneRiftwalker;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TideOfWar.class, HumbleBudoka.class, KitsuneRiftwalker.class, SongOfTheDryads.class})
class TideOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking sacrifices either every blocking creature (win) or every blocked creature (loss)")
    void coinFlipSacrificesExactlyOneSide() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player1, new TideOfWar());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        boolean blockerSacrificed = countPermanents(player2, "Kitsune Riftwalker") == 0
                && countPermanents(player1, "Humble Budoka") == 1;
        boolean attackerSacrificed = countPermanents(player1, "Humble Budoka") == 0
                && countPermanents(player2, "Kitsune Riftwalker") == 1;

        assertThat(blockerSacrificed != attackerSacrificed)
                .as("exactly one of the blocking or blocked creature is sacrificed")
                .isTrue();
        if (blockerSacrificed) {
            harness.assertInGraveyard(player2, "Kitsune Riftwalker");
        } else {
            harness.assertInGraveyard(player1, "Humble Budoka");
        }
    }

    @Test
    @DisplayName("Every creature on the losing side is sacrificed, not just one")
    void sacrificesEveryCreatureOnTheLosingSide() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player1, new TideOfWar());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        boolean blockersSacrificed = countPermanents(player2, "Kitsune Riftwalker") == 0
                && countPermanents(player1, "Humble Budoka") == 2;
        boolean blockedSacrificed = countPermanents(player1, "Humble Budoka") == 0
                && countPermanents(player2, "Kitsune Riftwalker") == 2;

        assertThat(blockersSacrificed != blockedSacrificed)
                .as("both blockers or both blocked attackers are sacrificed")
                .isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker survives either branch")
    void unblockedAttackerIsUntouched() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player1, new KitsuneRiftwalker());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player1, new TideOfWar());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Kitsune Riftwalker");
    }

    @Test
    @DisplayName("A surviving attacker remains blocked after Tide of War sacrifices its blocker")
    void survivingAttackerRemainsBlockedAfterBlockerIsSacrificed() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player1, new TideOfWar());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.getLife(player2.getId()))
                .as("a blocked attacker must not deal combat damage after its blocker is sacrificed")
                .isEqualTo(20);
    }

    @Test
    @DisplayName("Declaring no blockers does not trigger Tide of War")
    void noBlockersNoTrigger() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player1, new TideOfWar());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Tide of War"));
        harness.assertOnBattlefield(player1, "Humble Budoka");
        harness.assertOnBattlefield(player2, "Kitsune Riftwalker");
    }

    @Test
    @DisplayName("Tide of War triggers once per block declaration, however many creatures blocked")
    void triggersOnceRegardlessOfBlockerCount() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player1, new TideOfWar());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        assertThat(gd.stack.stream().filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Tide of War"))).hasSize(1);
    }

    @Test
    @DisplayName("Tide of War triggers when its controller controls only the blocker")
    void triggersWhenControllerControlsOnlyTheBlocker() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player2, new TideOfWar());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Humble Budoka")
                + countPermanents(player2, "Kitsune Riftwalker"))
                .as("one of the two combatants is sacrificed")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Two creatures blocking one attacker are both sacrificed on a win")
    void multipleBlockersOfOneAttackerAreSacrificedTogether() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player2, new KitsuneRiftwalker());
        addCreatureReady(player2, new HumbleBudoka());
        harness.addToBattlefield(player1, new TideOfWar());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        if (gameLogContains("wins the coin flip for Tide of War")) {
            assertThat(countPermanents(player2, "Kitsune Riftwalker")).isZero();
            harness.assertOnBattlefield(player1, "Humble Budoka");
        } else {
            harness.assertInGraveyard(player1, "Humble Budoka");
            assertThat(countPermanents(player2, "Kitsune Riftwalker")).isEqualTo(2);
        }
        harness.assertOnBattlefield(player2, "Humble Budoka");
        harness.assertOnBattlefield(player1, "Tide of War");
    }

    @Test
    @DisplayName("Tide of War enchanted by Song of the Dryads does not trigger")
    void noTriggerAfterPrintedAbilitiesAreRemoved() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new KitsuneRiftwalker());
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new TideOfWar());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, tide.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Song of the Dryads");

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Tide of War"));
    }
}
