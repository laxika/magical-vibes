package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.c.CapashenTemplar;
import com.github.laxika.magicalvibes.cards.e.EnchantmentAlteration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Disappear.class, CapashenTemplar.class, BraidwoodCup.class, EnchantmentAlteration.class})
class DisappearTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Disappear returns the creature and Aura to their owners' hands")
    void returnsCreatureAndAuraToTheirOwnersHands() {
        Permanent templar = addCreatureReady(player2, new CapashenTemplar());

        harness.setHand(player1, List.of(new Disappear()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, templar.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Disappear");
        harness.assertInHand(player2, "Capashen Templar");
        harness.assertNotOnBattlefield(player1, "Disappear");
        harness.assertNotOnBattlefield(player2, "Capashen Templar");
    }

    @Test
    @DisplayName("Disappear can enchant only a creature")
    void cannotEnchantNonCreature() {
        Permanent cup = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());

        harness.setHand(player1, List.of(new Disappear()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, cup.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A moved Disappear returns the creature it enchants when the ability resolves")
    void returnsCurrentEnchantedCreatureAfterMoving() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new CapashenTemplar());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new CapashenTemplar());
        harness.setHand(player1, List.of(new Disappear(), new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Disappear");
        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(original).doesNotContain(destination);
        assertThat(gd.playerHands.get(player2.getId())).contains(destination.getCard()).doesNotContain(original.getCard());
        harness.assertInHand(player1, "Disappear");
    }

    @Test
    @DisplayName("Multiple activations resolve after the Aura and creature have left")
    void multipleActivationsDoNotReturnCardsTwice() {
        harness.setHand(player2, List.of());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CapashenTemplar());
        harness.setHand(player1, List.of(new Disappear()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature.getCard());
        harness.assertInHand(player1, "Disappear");
        harness.assertNotOnBattlefield(player1, "Disappear");
        harness.assertNotOnBattlefield(player2, "Capashen Templar");
    }

    @Test
    @DisplayName("Other Auras on the returned creature go to the graveyard")
    void doesNotReturnOtherAttachedAuras() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CapashenTemplar());
        harness.setHand(player1, List.of(new Disappear(), new Disappear()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Disappear");
        harness.assertInGraveyard(player1, "Disappear");
        harness.assertInHand(player2, "Capashen Templar");
        harness.assertNotOnBattlefield(player1, "Disappear");
    }
}
