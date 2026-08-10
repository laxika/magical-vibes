package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BequeathalTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when the enchanted creature dies")
    void drawsTwoCardsWhenEnchantedCreatureDies() {
        addCreatureWithAura(player1, player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyEnchantedCreature(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Aura controller draws when an opponent's enchanted creature dies")
    void auraControllerDrawsWhenOpponentCreatureDies() {
        Permanent creature = addCreatureWithAura(player2, player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int auraControllerHandBefore = gd.playerHands.get(player1.getId()).size();

        destroyCreature(creature, player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHandBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger when a different creature dies")
    void doesNotTriggerWhenDifferentCreatureDies() {
        Permanent enchantedCreature = addCreatureWithAura(player1, player1);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyCreature(otherCreature, player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(enchantedCreature.getId()));
    }

    private Permanent addCreatureWithAura(Player creatureController, Player auraController) {
        harness.addToBattlefield(creatureController, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(creatureController.getId()).getFirst();

        Permanent aura = new Permanent(new Bequeathal());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return creature;
    }

    private void destroyEnchantedCreature(Player creatureController) {
        Permanent creature = gd.playerBattlefields.get(creatureController.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        destroyCreature(creature, creatureController == player1 ? player2 : player1);
    }

    private void destroyCreature(Permanent creature, Player spellController) {
        harness.forceActivePlayer(spellController);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(spellController, List.of(new DoomBlade()));
        harness.addMana(spellController, ManaColor.BLACK, 2);
        harness.castInstant(spellController, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
