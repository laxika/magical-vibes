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
