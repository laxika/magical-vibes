package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CastingOfBonesTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, the Aura's controller draws three cards then discards one")
    void deathTriggerDrawsThreeAndDiscardsOne() {
        Permanent creature = addCreatureWithAura(player1, player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        destroyWithDoomBlade(player2, creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The Aura's controller draws even when it enchants an opponent's creature")
    void auraControllerDrawsWhenEnchantingOpponentCreature() {
        Permanent creature = addCreatureWithAura(player2, player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        destroyWithDoomBlade(player1, creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when a creature other than the enchanted one dies")
    void noTriggerWhenDifferentCreatureDies() {
        Permanent enchanted = addCreatureWithAura(player1, player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent other = gd.playerBattlefields.get(player2.getId()).getFirst();

        destroyWithDoomBlade(player1, other);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(enchanted.getId()));
    }

    private void destroyWithDoomBlade(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities(); // resolve Doom Blade — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve the death trigger
    }

    /**
     * Places a Grizzly Bears on the creature controller's battlefield and attaches a
     * Casting of Bones controlled by the aura controller.
     *
     * @return the Grizzly Bears permanent
     */
    private Permanent addCreatureWithAura(Player creatureController, Player auraController) {
        harness.addToBattlefield(creatureController, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(creatureController.getId()).getFirst();

        Card auraCard = new CastingOfBones();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);

        return creature;
    }
}
