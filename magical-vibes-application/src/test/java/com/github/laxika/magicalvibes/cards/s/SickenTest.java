package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sicken.class, GorillaWarrior.class, SandbarMerfolk.class, Forest.class, ClawsOfGix.class})
class SickenTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -1/-1")
    void shrinksEnchantedCreature() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());

        harness.setHand(player1, List.of(new Sicken()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, gorilla.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gorilla)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sicken puts a creature with one toughness into its graveyard")
    void killsCreatureWithZeroToughness() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new SandbarMerfolk());

        harness.setHand(player1, List.of(new Sicken()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, merfolk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sandbar Merfolk");
        harness.assertInGraveyard(player1, "Sicken");
    }

    @Test
    @DisplayName("Cycling discards Sicken and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Sicken()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sicken");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Sicken cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ClawsOfGix());
        harness.setHand(player1, List.of(new Sicken()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Sicken can enchant your creature and leaves other creatures unaffected")
    void enchantsOwnCreatureOnly() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        harness.setHand(player1, List.of(new Sicken()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling discards as a cost and draws only when the ability resolves")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new Sicken()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Sicken");
        harness.assertNotInHand(player1, "Sicken");
        harness.assertNotInHand(player1, "Forest");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Sicken");
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new Sicken()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Sicken");
        harness.assertNotInGraveyard(player1, "Sicken");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }
}
