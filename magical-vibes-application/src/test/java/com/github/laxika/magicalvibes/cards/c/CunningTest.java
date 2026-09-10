package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NullBrooch;
import com.github.laxika.magicalvibes.cards.w.WhiptongueFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cunning.class, NullBrooch.class, WhiptongueFrog.class})
class CunningTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent frog = addReadyCreature(player1);
        enchant(frog, player2);

        assertThat(gqs.getEffectivePower(gd, frog)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, frog)).isEqualTo(6);
    }

    @Test
    @DisplayName("Attacking enchanted creature sacrifices Cunning at the next cleanup")
    void attackingEnchantedCreatureSacrificesAuraAtCleanup() {
        Permanent frog = addReadyCreature(player1);
        enchant(frog, player2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cunning");

        cleanup();

        harness.assertNotOnBattlefield(player2, "Cunning");
        harness.assertInGraveyard(player2, "Cunning");
    }

    @Test
    @DisplayName("Attacking a different creature does not sacrifice Cunning")
    void attackingDifferentCreatureDoesNotSacrificeAura() {
        Permanent enchantedCreature = addReadyCreature(player1);
        enchant(enchantedCreature, player2);
        addReadyCreature(player1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        cleanup();

        harness.assertOnBattlefield(player2, "Cunning");
        harness.assertNotInGraveyard(player2, "Cunning");
    }

    @Test
    @DisplayName("Blocking enchanted creature sacrifices Cunning at the next cleanup")
    void blockingEnchantedCreatureSacrificesAuraAtCleanup() {
        Permanent enchantedCreature = addReadyCreature(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        enchant(enchantedCreature, player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cunning");
        harness.assertOnBattlefield(player2, "Whiptongue Frog");

        cleanup();

        harness.assertNotOnBattlefield(player1, "Cunning");
        harness.assertInGraveyard(player1, "Cunning");
        harness.assertOnBattlefield(player2, "Whiptongue Frog");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new NullBrooch());
        harness.setHand(player1, List.of(new Cunning()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent artifact = findPermanent(player1, "Null Brooch");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new WhiptongueFrog());
    }

    private Permanent enchant(Permanent creature, Player controller) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new Cunning());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void cleanup() {
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
    }

}
