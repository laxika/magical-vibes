package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cessation.class, GiantCockroach.class, GrimMonolith.class})
class CessationTest extends BaseCardTest {
    @Test
    void returnsToOwnersHandWhenControlledByOpponent() {
        Cessation card = new Cessation();
        card.setOwnerId(player1.getId());
        Permanent cessation = new Permanent(card);
        gd.playerBattlefields.get(player2.getId()).add(cessation);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, cessation));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(handCard -> handCard.getId().equals(card.getId()))
                .toList()).containsExactly(card);
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .noneMatch(handCard -> handCard.getId().equals(card.getId()))).isTrue();
    }

    @Test
    @DisplayName("Cessation attaches to a creature and prevents it from attacking")
    void attachesAndPreventsAttacking() {
        Permanent creature = addCreatureReady(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new Cessation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent cessation = findPermanent(player1, "Cessation");
        assertThat(cessation.getAttachedTo()).isEqualTo(creature.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cessation does not prevent the enchanted creature from blocking")
    void enchantedCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new GiantCockroach());
        Permanent cessation = new Permanent(new Cessation());
        cessation.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player1.getId()).add(cessation);

        Permanent attacker = addCreatureReady(player1, new GiantCockroach());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cessation returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent creature = addCreatureReady(player1, new GiantCockroach());
        Permanent cessation = new Permanent(new Cessation());
        cessation.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(cessation);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, cessation));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cessation");
        harness.assertNotInGraveyard(player1, "Cessation");
        harness.assertNotOnBattlefield(player1, "Cessation");
    }

    @Test
    @DisplayName("Cessation cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        harness.setHand(player1, List.of(new Cessation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void returnsWhenEnchantedCreatureLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new GiantCockroach());
        Permanent cessation = new Permanent(new Cessation());
        cessation.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(cessation);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInHand(player1, cessation.getCard().getName());
        harness.assertNotInGraveyard(player1, cessation.getCard().getName());
        harness.assertNotOnBattlefield(player1, cessation.getCard().getName());
    }
}
