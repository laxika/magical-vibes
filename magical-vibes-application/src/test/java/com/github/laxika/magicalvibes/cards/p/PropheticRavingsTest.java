package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BrazenWolves;
import com.github.laxika.magicalvibes.cards.c.CatharsShield;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PropheticRavings.class, BrazenWolves.class, CatharsShield.class})
class PropheticRavingsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has haste and can loot")
    void enchantedCreatureHasHasteAndCanLoot() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BrazenWolves());
        bears.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PropheticRavings());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        harness.setHand(player1, List.of(new CatharsShield()));
        harness.setLibrary(player1, List.of(new CatharsShield()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Cathar's Shield");
        harness.assertInHand(player1, "Cathar's Shield");
    }

    @Test
    @DisplayName("Removing Prophetic Ravings removes haste and the granted ability")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BrazenWolves());
        bears.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PropheticRavings());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void hasteAllowsLootingImmediatelyAndDiscardIsPaidBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BrazenWolves());
        creature.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PropheticRavings());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new CatharsShield()));
        harness.setLibrary(player1, List.of(new BrazenWolves()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Cathar's Shield");

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Brazen Wolves");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotLootWithoutACardToDiscard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BrazenWolves());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PropheticRavings());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CatharsShield()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsCreatureCanBeEnchantedAndItsControllerLoots() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrazenWolves());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new PropheticRavings()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        harness.setHand(player2, List.of(new CatharsShield()));
        harness.setLibrary(player2, List.of(new BrazenWolves()));
        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cathar's Shield");
        harness.assertInHand(player2, "Brazen Wolves");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Prophetic Ravings cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new CatharsShield());
        harness.setHand(player1, List.of(new PropheticRavings()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = findPermanent(player1, "Cathar's Shield");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
