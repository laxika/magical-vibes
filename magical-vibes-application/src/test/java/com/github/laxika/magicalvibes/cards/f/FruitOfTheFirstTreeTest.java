package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.Hushbringer;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FruitOfTheFirstTree.class, GiantSpider.class, FountainOfYouth.class, NevinyrralsDisk.class, Hushbringer.class})
class FruitOfTheFirstTreeTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted creature dies, you gain life and draw cards equal to its toughness")
    void enchantedCreatureDeathGainsLifeAndDrawsToughness() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent fruit = harness.addToBattlefieldAndReturn(player1, new FruitOfTheFirstTree());
        fruit.setAttachedTo(spider.getId());

        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 4);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FruitOfTheFirstTree()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Uses modified toughness immediately before death rather than printed toughness")
    void usesLastKnownModifiedToughness() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        spider.setToughnessModifier(3);
        harness.setHand(player1, List.of(new FruitOfTheFirstTree()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fruit of the First Tree").getAttachedTo()).isEqualTo(spider.getId());
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        spider.setMarkedDamage(7);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 7);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 7);
    }

    @Test
    @DisplayName("A creature dying with zero toughness grants no life or cards")
    void zeroToughnessGrantsNothing() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent fruit = harness.addToBattlefieldAndReturn(player1, new FruitOfTheFirstTree());
        fruit.setAttachedTo(spider.getId());
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        spider.setToughnessModifier(-4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spider.getCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Triggers when the Aura and enchanted creature are destroyed simultaneously")
    void simultaneousDestructionStillTriggers() {
        harness.addToBattlefield(player1, new NevinyrralsDisk());
        Permanent fruit = harness.addToBattlefieldAndReturn(player1, new FruitOfTheFirstTree());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        fruit.setAttachedTo(spider.getId());
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fruit of the First Tree");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
    }

    @Test
    @DisplayName("Hushbringer prevents the enchanted creature's death from triggering the Aura")
    void deathTriggerIsSuppressedByHushbringer() {
        harness.addToBattlefield(player2, new Hushbringer());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent fruit = harness.addToBattlefieldAndReturn(player1, new FruitOfTheFirstTree());
        fruit.setAttachedTo(spider.getId());
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spider.getCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
