package com.github.laxika.magicalvibes.cards.u;

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

class UnhallowedPactTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, it returns under the Aura controller's control")
    void returnsUnderAuraControllersControl() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Card creatureCard = creature.getCard();

        castUnhallowedPact(player1, creature);
        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The Aura's own creature comes back under its controller too")
    void returnsOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card creatureCard = creature.getCard();

        castUnhallowedPact(player1, creature);
        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Unhallowed Pact goes to the graveyard when the enchanted creature dies")
    void auraGoesToGraveyardOnDeath() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castUnhallowedPact(player1, creature);
        killCreature(creature);

        harness.assertInGraveyard(player1, "Unhallowed Pact");
        harness.assertNotOnBattlefield(player1, "Unhallowed Pact");
    }

    @Test
    @DisplayName("Unhallowed Pact cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent nonCreature = new Permanent(new UnhallowedPact());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);

        harness.setHand(player1, List.of(new UnhallowedPact()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castUnhallowedPact(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new UnhallowedPact()));
        harness.addMana(controller, ManaColor.BLACK, 3);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade; the creature dies and the trigger goes on the stack
        harness.passBothPriorities(); // resolve the return trigger
    }
}
