package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.TurnStep;
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

@CardUsed({CartoucheOfZeal.class, Colossapede.class})
class CartoucheOfZealTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and has haste")
    void enchantedCreatureBoostedAndHaste() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new Colossapede());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CartoucheOfZeal());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and haste when the Cartouche is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new Colossapede());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CartoucheOfZeal());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("ETB can't-block hits the targeted creature, not the enchanted creature")
    void etbCantBlockHitsTargetNotEnchanted() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent opponentBlocker = harness.addToBattlefieldAndReturn(player2, new Colossapede());

        harness.setHand(player1, List.of(new CartoucheOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, mine.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBlocker.getId());
        harness.passBothPriorities();

        assertThat(opponentBlocker.isCantBlockThisTurn()).isTrue();
        // The enchant target (group 0) must NOT be marked can't-block.
        assertThat(mine.isCantBlockThisTurn()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cartouche of Zeal")
                        && mine.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("The enters trigger can target the enchanted creature")
    void etbCanTargetEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.setHand(player1, List.of(new CartoucheOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The enters trigger resolves even after the Aura leaves")
    void etbResolvesWithoutAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new CartoucheOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blocker.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof CartoucheOfZeal);
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        assertThat(creature.isCantBlockThisTurn()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("The blocking restriction expires after this turn")
    void blockingRestrictionExpires() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new CartoucheOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a creature you don't control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        // A creature you control makes the Aura playable, so casting reaches target validation.
        harness.addToBattlefield(player1, new Colossapede());

        harness.setHand(player1, List.of(new CartoucheOfZeal()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
