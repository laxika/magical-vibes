package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BesottedKnight;
import com.github.laxika.magicalvibes.cards.b.BetrothTheBeast;
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

@CardUsed({CoopedUp.class, BesottedKnight.class, BetrothTheBeast.class, CandyTrail.class})
class CoopedUpTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack or block")
    void enchantedCreatureCannotAttackOrBlock() {
        Permanent enchanted = addCreatureReady(player2, new BesottedKnight());
        attachAura(player1, enchanted);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        addCreatureReady(player1, new BesottedKnight()).setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Activating Cooped Up exiles the enchanted creature")
    void activatingAbilityExilesEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new BesottedKnight());
        Permanent aura = attachAura(player1, enchanted);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchanted.getCard());
        harness.assertInGraveyard(player1, "Cooped Up");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        harness.setHand(player1, List.of(new CoopedUp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void castingAuraAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new CoopedUp()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cooped Up").getAttachedTo()).isEqualTo(creature.getId());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void exileUsesCurrentEnchantedCreatureAfterAuraMoves() {
        Permanent original = addCreatureReady(player2, new BesottedKnight());
        Permanent current = addCreatureReady(player2, new BesottedKnight());
        Permanent aura = attachAura(player1, original);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, null);

        aura.setAttachedTo(current.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(original).doesNotContain(current);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(current.getCard()).doesNotContain(original.getCard());
        harness.assertInGraveyard(player1, "Cooped Up");
    }

    @Test
    void exileStillResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2, new BesottedKnight());
        Permanent aura = attachAura(player1, creature);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
    }

    private Permanent attachAura(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = new Permanent(new CoopedUp());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
