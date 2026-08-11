package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolisCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Polis Crusher has protection from enchantments")
    void hasProtectionFromEnchantments() {
        Permanent crusher = addReadyCrusher();
        Permanent enchantment = addPermanent(player2, createCard("Test Enchantment", CardType.ENCHANTMENT));
        Permanent creature = addCreatureReady(player2, createCard("Test Creature", CardType.CREATURE));

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, crusher, enchantment)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, crusher, creature)).isFalse();
    }

    @Test
    @DisplayName("Polis Crusher becomes monstrous and destroys an enchantment controlled by the damaged player")
    void monstrousCombatDamageDestroysDamagedPlayersEnchantment() {
        Permanent crusher = addReadyCrusher();
        Permanent enchantment = addPermanent(player2, createCard("Test Enchantment", CardType.ENCHANTMENT));
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        crusher.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(enchantment.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(enchantment.getId()));

        harness.assertNotOnBattlefield(player2, "Test Enchantment");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Polis Crusher does not trigger before it becomes monstrous")
    void combatDamageBeforeMonstrousDoesNotDestroyAnEnchantment() {
        Permanent crusher = addReadyCrusher();
        addPermanent(player2, createCard("Test Enchantment", CardType.ENCHANTMENT));
        crusher.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Test Enchantment");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyCrusher() {
        Permanent crusher = addCreatureReady(player1, new PolisCrusher());
        crusher.setSummoningSick(false);
        return crusher;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Card createCard(String name, CardType type) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(type);
        return card;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
