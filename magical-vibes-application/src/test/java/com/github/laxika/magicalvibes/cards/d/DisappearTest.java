package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.c.CapashenTemplar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disappear.class, CapashenTemplar.class, BraidwoodCup.class})
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
}
