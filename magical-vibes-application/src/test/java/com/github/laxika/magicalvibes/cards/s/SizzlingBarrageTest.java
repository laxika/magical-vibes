package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KazanduStomper;
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

@CardUsed({SizzlingBarrage.class, CliffhavenSellSword.class, KazanduStomper.class, IntoTheRoil.class})
class SizzlingBarrageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a creature that blocked earlier this turn")
    void damagesCreatureThatBlockedEarlierThisTurn() {
        Permanent attacker = addCreatureReady(player1, new CliffhavenSellSword());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CliffhavenSellSword());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        attacker.setAttacking(false);
        blocker.setBlocking(false);
        blocker.getBlockingTargetIds().clear();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SizzlingBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, blocker.getId());

        harness.assertNotOnBattlefield(player2, "Cliffhaven Sell-Sword");
        harness.assertInGraveyard(player2, "Cliffhaven Sell-Sword");
    }

    @Test
    @DisplayName("Cannot target a creature that was blocked rather than one that blocked")
    void cannotTargetCreatureThatWasBlocked() {
        Permanent attacker = addCreatureReady(player1, new CliffhavenSellSword());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CliffhavenSellSword());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        attacker.setAttacking(false);
        blocker.setBlocking(false);
        blocker.getBlockingTargetIds().clear();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SizzlingBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature that blocked this turn");
    }

    @Test
    @DisplayName("Deals exactly four damage to your own creature while it is still blocking")
    void dealsExactlyFourDamageToCurrentBlocker() {
        addCreatureReady(player2, new CliffhavenSellSword());
        Permanent blocker = addCreatureReady(player1, new KazanduStomper());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))));

        harness.setHand(player1, List.of(new SizzlingBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.castAndResolveInstant(player1, 0, blocker.getId()));

        harness.assertOnBattlefield(player1, "Kazandu Stomper");
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature that never blocked this turn")
    void cannotTargetCreatureThatNeverBlocked() {
        Permanent creature = addCreatureReady(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new SizzlingBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature that blocked this turn");
    }

    @Test
    @DisplayName("A creature that blocked on the previous turn is no longer a legal target")
    void blockingHistoryExpiresOnNextTurn() {
        addCreatureReady(player1, new CliffhavenSellSword());
        Permanent blocker = addCreatureReady(player2, new KazanduStomper());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertOnBattlefield(player2, "Kazandu Stomper");

        harness.setHand(player2, List.of(new SizzlingBarrage()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature that blocked this turn");
    }

    @Test
    @DisplayName("Does not deal damage when the blocker leaves before resolution")
    void targetLeavingBeforeResolutionMakesSpellFailToResolve() {
        addCreatureReady(player1, new CliffhavenSellSword());
        Permanent blocker = addCreatureReady(player2, new KazanduStomper());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.setHand(player1, List.of(new SizzlingBarrage()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.castInstant(player1, 0, blocker.getId());
            harness.castAndResolveInstant(player2, 0, blocker.getId());
            harness.assertInHand(player2, "Kazandu Stomper");
            harness.passBothPriorities();
        });

        harness.assertNotOnBattlefield(player2, "Kazandu Stomper");
        harness.assertInHand(player2, "Kazandu Stomper");
        harness.assertInGraveyard(player1, "Sizzling Barrage");
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
