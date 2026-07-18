package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BackfireTest extends BaseCardTest {

    /** Enchants the opponent's creature with Backfire (controlled by player1). */
    private Permanent enchantOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Backfire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        return bears;
    }

    @Test
    @DisplayName("When the enchanted creature deals combat damage to you, that much is dealt to its controller")
    void reflectsCombatDamageToItsController() {
        enchantOpponentCreature();

        int creatureControllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        // player2 attacks player1 (the aura's controller) with the enchanted 2/2.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        while (gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Backfire"))) {
            harness.passBothPriorities();
        }

        // 2 combat damage dealt to player1 → Backfire deals 2 to player2 (the creature's controller).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(creatureControllerLifeBefore - 2);
    }

    @Test
    @DisplayName("No reflection when the enchanted creature deals no damage to you")
    void noReflectionWithoutDamage() {
        enchantOpponentCreature();

        int creatureControllerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Backfire"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(creatureControllerLifeBefore);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // a legal creature target must exist
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Backfire()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
