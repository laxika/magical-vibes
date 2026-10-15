package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bequeathal.class, RagingGoblin.class, CityOfTraitors.class})
class BequeathalTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when the enchanted creature dies")
    void drawsTwoCardsWhenEnchantedCreatureDies() {
        Permanent enchantedCreature = addCreatureWithAura(player1, player1);
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyCreature(enchantedCreature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Aura controller draws when an opponent's enchanted creature dies")
    void auraControllerDrawsWhenOpponentCreatureDies() {
        Permanent creature = addCreatureWithAura(player2, player1);
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        int auraControllerHandBefore = gd.playerHands.get(player1.getId()).size();

        destroyCreature(creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHandBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger when a different creature dies")
    void doesNotTriggerWhenDifferentCreatureDies() {
        Permanent enchantedCreature = addCreatureWithAura(player1, player1);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyCreature(otherCreature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(enchantedCreature.getId()));
    }

    @Test
    @DisplayName("Attaches to a target creature when cast")
    void attachesToTargetCreatureWhenCast() {
        Permanent creature = addCreatureReady(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new Bequeathal()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Bequeathal
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        harness.setHand(player1, List.of(new Bequeathal()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Draw waits for the death trigger to resolve after the Aura leaves the battlefield")
    void drawsOnlyWhenDeathTriggerResolves() {
        Permanent creature = addCreatureWithAura(player2, player1);
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        creature.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Bequeathal");
        harness.assertNotOnBattlefield(player1, "Bequeathal");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    @DisplayName("Each Bequeathal on the same creature draws two cards")
    void multipleAurasEachDrawTwoCards() {
        Permanent creature = addCreatureWithAura(player1, player1);
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new Bequeathal());
        secondAura.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(
                new RagingGoblin(), new RagingGoblin(), new RagingGoblin(), new RagingGoblin()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
        harness.assertNotOnBattlefield(player1, "Bequeathal");
    }

    @Test
    @DisplayName("Does not draw when enchanted creature returns to hand")
    void doesNotDrawWhenEnchantedCreatureReturnsToHand() {
        Permanent creature = addCreatureWithAura(player2, player1);
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.getPermanentRemovalService().removePermanentToHand(gd, creature);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Raging Goblin");
        harness.assertInGraveyard(player1, "Bequeathal");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not draw when Bequeathal is destroyed before the creature dies")
    void doesNotDrawWhenAuraLeavesBeforeCreatureDies() {
        Permanent creature = addCreatureWithAura(player1, player1);
        Permanent aura = findPermanent(player1, "Bequeathal");
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, aura);
        destroyCreature(creature);

        harness.assertInGraveyard(player1, "Bequeathal");
        harness.assertInGraveyard(player1, "Raging Goblin");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Draws two cards when enchanted creature is sacrificed")
    void drawsWhenEnchantedCreatureIsSacrificed() {
        Permanent creature = addCreatureWithAura(player2, player1);
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Raging Goblin");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not draw when enchanted creature is exiled")
    void doesNotDrawWhenEnchantedCreatureIsExiled() {
        Permanent creature = addCreatureWithAura(player2, player1);
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.getPermanentRemovalService().removePermanentToExile(gd, creature);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertNotInGraveyard(player2, "Raging Goblin");
        harness.assertInGraveyard(player1, "Bequeathal");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private Permanent addCreatureWithAura(Player creatureController, Player auraController) {
        Permanent creature = addCreatureReady(creatureController, new RagingGoblin());
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new Bequeathal());
        aura.setAttachedTo(creature.getId());
        return creature;
    }

    private void destroyCreature(Permanent creature) {
        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
