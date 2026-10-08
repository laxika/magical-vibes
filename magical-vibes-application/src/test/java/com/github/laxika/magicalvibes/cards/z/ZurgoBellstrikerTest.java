package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.k.KeeperOfTheLens;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZurgoBellstriker.class, KeeperOfTheLens.class, ColossodonYearling.class})
class ZurgoBellstrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Zurgo can block a creature with power 1")
    void canBlockPowerOne() {
        Permanent zurgo = addCreatureReady(player2, new ZurgoBellstriker());
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheLens());
        keeper.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(zurgo),
                gd.playerBattlefields.get(player1.getId()).indexOf(keeper))));

        assertThat(zurgo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Zurgo can't block a creature with power 2 or greater")
    void cannotBlockPowerTwoOrGreater() {
        Permanent zurgo = addCreatureReady(player2, new ZurgoBellstriker());
        Permanent yearling = addCreatureReady(player1, new ColossodonYearling());
        yearling.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(zurgo),
                gd.playerBattlefields.get(player1.getId()).indexOf(yearling)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A normal cast does not return Zurgo to its owner's hand at end step")
    void normalCastDoesNotReturnAtEndStep() {
        harness.setHand(player1, List.of(new ZurgoBellstriker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent zurgo = findPermanent(player1, "Zurgo Bellstriker");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(findPermanent(player1, "Zurgo Bellstriker")).isSameAs(zurgo);
    }

    @Test
    @DisplayName("Dash grants haste and returns Zurgo to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new ZurgoBellstriker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent zurgo = findPermanent(player1, "Zurgo Bellstriker");
        assertThat(zurgo.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        harness.assertInHand(player1, "Zurgo Bellstriker");
        harness.assertNotOnBattlefield(player1, "Zurgo Bellstriker");
    }

    @Test
    @DisplayName("Dash does not create an enters-the-battlefield triggered ability")
    void dashDoesNotCreateAnEnterBattlefieldTrigger() {
        harness.setHand(player1, List.of(new ZurgoBellstriker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zurgo Bellstriker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dash creates exactly one return trigger at the next end step")
    void dashCreatesOnlyOneEndStepReturnTrigger() {
        harness.setHand(player1, List.of(new ZurgoBellstriker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Zurgo Bellstriker");
        harness.assertNotInHand(player1, "Zurgo Bellstriker");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInHand(player1, "Zurgo Bellstriker");
        harness.assertNotOnBattlefield(player1, "Zurgo Bellstriker");
    }
}
