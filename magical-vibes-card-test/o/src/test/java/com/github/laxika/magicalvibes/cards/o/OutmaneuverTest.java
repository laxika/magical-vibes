package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Outmaneuver.class, CoralMerfolk.class})
class OutmaneuverTest extends BaseCardTest {

    @Test
    @DisplayName("Targeted blocked creatures deal combat damage to what they attacked")
    void targetedBlockedCreaturesAssignDamageAsThoughUnblocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1);
        Permanent blocker = addCreature(player2);
        block(attacker, blocker);

        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantForX(player1, 0, 1, List.of(attacker.getId()));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Outmaneuver affects exactly the chosen blocked creatures")
    void affectsOnlyChosenBlockedCreatures() {
        harness.setLife(player2, 20);
        Permanent targetedAttacker = addAttacker(player1);
        Permanent untargetedAttacker = addAttacker(player1);
        Permanent targetedBlocker = addCreature(player2);
        Permanent untargetedBlocker = addCreature(player2);
        block(targetedAttacker, targetedBlocker);
        block(untargetedAttacker, untargetedBlocker);

        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantForX(player1, 0, 1, List.of(targetedAttacker.getId()));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Coral Merfolk"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Outmaneuver applies to every selected blocked creature")
    void appliesToEverySelectedBlockedCreature() {
        harness.setLife(player2, 20);
        Permanent firstAttacker = addAttacker(player1);
        Permanent secondAttacker = addAttacker(player1);
        Permanent firstBlocker = addCreature(player2);
        Permanent secondBlocker = addCreature(player2);
        block(firstAttacker, firstBlocker);
        block(secondAttacker, secondBlocker);

        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantForX(player1, 0, 2, List.of(firstAttacker.getId(), secondAttacker.getId()));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Coral Merfolk"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Outmaneuver works when a blocked creature has no blockers left")
    void worksWhenBlockedCreatureHasNoBlockersLeft() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1);
        attacker.setBlockedWithoutBlockers(true);

        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantForX(player1, 0, 1, List.of(attacker.getId()));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Must choose exactly X blocked creatures")
    void requiresExactlyXTargets() {
        Permanent attacker = addAttacker(player1);
        Permanent blocker = addCreature(player2);
        block(attacker, blocker);

        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Outmaneuver can target only blocked creatures")
    void canTargetOnlyBlockedCreatures() {
        Permanent attacker = addAttacker(player1);

        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Targets must be blocked creatures");
    }

    @Test
    @DisplayName("Outmaneuver can be cast with X zero and no targets")
    void canBeCastWithZeroTargets() {
        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Outmaneuver");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The defending player can target opposing blocked creatures and blockers still deal damage")
    void defenderCanTargetOpposingBlockedCreature() {
        Permanent attacker = addAttacker(player1);
        Permanent blocker = addCreature(player2);
        block(attacker, blocker);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Outmaneuver()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        harness.castInstantForX(player2, 0, 1, List.of(attacker.getId()));
        resolveAllTriggers();
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        harness.assertNotOnBattlefield(player1, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Coral Merfolk");
    }

    @Test
    @DisplayName("A blocking creature is not a legal target")
    void cannotTargetBlockingCreature() {
        Permanent attacker = addAttacker(player1);
        Permanent blocker = addCreature(player2);
        block(attacker, blocker);
        harness.setHand(player1, List.of(new Outmaneuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(blocker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Targets must be blocked creatures");
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addCreature(player);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new CoralMerfolk());
    }

    private void block(Permanent attacker, Permanent blocker) {
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        blocker.addBlockingTargetId(attacker.getId());
    }
}
