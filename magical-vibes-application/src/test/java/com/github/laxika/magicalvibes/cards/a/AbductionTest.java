package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbductionTest extends BaseCardTest {

    // ===== Gaining control =====

    @Test
    @DisplayName("Resolving Abduction steals the enchanted creature")
    void stealsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2);

        castAbduction(player1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    // ===== ETB untap =====

    @Test
    @DisplayName("Abduction untaps the enchanted creature as it enters")
    void untapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        castAbduction(player1, creature);

        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Death trigger: return under owner's control =====

    @Test
    @DisplayName("When the enchanted creature dies, it returns to the battlefield under its owner's control")
    void returnsToOwnerBattlefieldOnDeath() {
        Permanent creature = addCreatureReady(player2);
        Card creatureCard = creature.getCard();

        // Player 1 steals the creature with Abduction
        castAbduction(player1, creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // The controller kills the stolen creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve the return trigger

        // The creature returns to its owner (player2), not the aura's controller (player1)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Abduction itself goes to its owner's graveyard when the enchanted creature dies")
    void auraGoesToGraveyardOnDeath() {
        Permanent creature = addCreatureReady(player2);

        castAbduction(player1, creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Abduction"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Abduction"));
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Abduction cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        // Put an enchantment (non-creature) permanent on the battlefield to target
        Permanent nonCreature = new Permanent(new Abduction());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);

        harness.setHand(player1, List.of(new Abduction()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void castAbduction(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new Abduction()));
        harness.addMana(controller, ManaColor.BLUE, 4);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities(); // resolve the aura (attach + gain control)
        harness.passBothPriorities(); // resolve the ETB untap trigger
    }

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
