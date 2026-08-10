package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CunningTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addReadyCreature(player1);
        enchant(bears, player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking enchanted creature sacrifices Cunning at the next cleanup")
    void attackingEnchantedCreatureSacrificesAuraAtCleanup() {
        Permanent bears = addReadyCreature(player1);
        enchant(bears, player2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cunning");

        cleanup();

        harness.assertNotOnBattlefield(player2, "Cunning");
        harness.assertInGraveyard(player2, "Cunning");
    }

    @Test
    @DisplayName("Blocking enchanted creature sacrifices Cunning at the next cleanup")
    void blockingEnchantedCreatureSacrificesAuraAtCleanup() {
        addReadyCreature(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        enchant(findPermanent(player2, "Grizzly Bears"), player1);

        declareBlockers(player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cunning");

        cleanup();

        harness.assertNotOnBattlefield(player1, "Cunning");
        harness.assertInGraveyard(player1, "Cunning");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Cunning()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent enchant(Permanent creature, Player controller) {
        Permanent aura = new Permanent(new Cunning());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void cleanup() {
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
    }

    private void declareBlockers(Player player, List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player, assignments);
    }
}
