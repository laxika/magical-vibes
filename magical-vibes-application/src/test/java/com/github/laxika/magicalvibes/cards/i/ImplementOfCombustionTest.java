package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImplementOfCombustionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it deals 1 damage to a player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Implement of Combustion");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrificing it deals 1 damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent walker = new Permanent(new GarrukWildspeaker());
        walker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(walker);
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, walker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draws a card when it is put into a graveyard from the battlefield")
    void drawsWhenPutIntoGraveyardFromBattlefield() {
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
