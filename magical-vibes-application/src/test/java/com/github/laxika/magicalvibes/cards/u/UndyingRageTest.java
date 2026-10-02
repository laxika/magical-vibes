package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndyingRage.class, BenalishCavalry.class, ChromaticStar.class})
class UndyingRageTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void grantsBoost() {
        Permanent creature = addCreatureReady(player1, new BenalishCavalry());
        attachUndyingRage(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can enchant a creature and attach to it when resolved")
    void enchantsCreatureWhenCast() {
        Permanent creature = addCreatureReady(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new UndyingRage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Undying Rage");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new BenalishCavalry());
        attachUndyingRage(player1, blocker);

        Permanent attacker = addCreatureReady(player1, new BenalishCavalry());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent creature = addCreatureReady(player1, new BenalishCavalry());
        Permanent aura = attachUndyingRage(player1, creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Undying Rage");
        harness.assertNotInGraveyard(player1, "Undying Rage");
        harness.assertNotOnBattlefield(player1, "Undying Rage");
    }

    @Test
    @DisplayName("Returns to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByOpponent() {
        Permanent creature = addCreatureReady(player2, new BenalishCavalry());
        UndyingRage card = new UndyingRage();
        card.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, card);
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Undying Rage");
        harness.assertNotInHand(player2, "Undying Rage");
        harness.assertNotInGraveyard(player1, "Undying Rage");
        harness.assertNotOnBattlefield(player2, "Undying Rage");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ChromaticStar());
        harness.setHand(player1, List.of(new UndyingRage()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachUndyingRage(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new UndyingRage());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
