package com.github.laxika.magicalvibes.cards.f;

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

class FontOfIreTest extends BaseCardTest {

    @Test
    @DisplayName("sacrificing Font of Ire deals 5 damage to a player")
    void sacrificingFontOfIreDealsFiveDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfIre());
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("can deal damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent walker = new Permanent(new GarrukWildspeaker());
        walker.setCounterCount(CounterType.LOYALTY, 6);
        gd.playerBattlefields.get(player2.getId()).add(walker);
        harness.addToBattlefield(player1, new FontOfIre());
        addActivationMana();

        harness.activateAbility(player1, 0, null, walker.getId());
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new FontOfIre());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
