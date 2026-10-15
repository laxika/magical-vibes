package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GracefulCat;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CartoucheOfAmbition.class, GracefulCat.class})
class CartoucheOfAmbitionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches to a creature you control and may put a -1/-1 counter on target creature")
    void resolvingAttachesAndPutsCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GracefulCat());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GracefulCat());

        harness.setHand(player1, List.of(new CartoucheOfAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cartouche of Ambition")
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(opponentBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may ability does not put a -1/-1 counter")
    void decliningMayDoesNotPutCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GracefulCat());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GracefulCat());

        harness.setHand(player1, List.of(new CartoucheOfAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(opponentBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and has lifelink")
    void enchantedCreatureBoostedAndLifelink() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GracefulCat());

        Permanent aura = new Permanent(new CartoucheOfAmbition());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and lifelink when the Cartouche is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GracefulCat());

        Permanent aura = new Permanent(new CartoucheOfAmbition());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The enter trigger can target the enchanted creature itself")
    void canPutCounterOnEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GracefulCat());
        harness.setHand(player1, List.of(new CartoucheOfAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The enter trigger resolves even after the Aura leaves the battlefield")
    void triggerResolvesAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GracefulCat());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GracefulCat());
        harness.setHand(player1, List.of(new CartoucheOfAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof CartoucheOfAmbition)
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a creature you don't control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GracefulCat());
        // A creature you control makes the Aura playable, so casting reaches target validation.
        harness.addToBattlefield(player1, new GracefulCat());

        harness.setHand(player1, List.of(new CartoucheOfAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
