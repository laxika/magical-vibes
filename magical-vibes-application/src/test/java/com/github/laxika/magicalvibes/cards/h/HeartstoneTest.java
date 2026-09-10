package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Carnassid;
import com.github.laxika.magicalvibes.cards.s.SliverQueen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Carnassid.class, Heartstone.class, HornetCannon.class, SliverQueen.class})
class HeartstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a creature's activated ability by one generic mana for any player")
    void reducesCreatureAbilityForAnyPlayer() {
        harness.addToBattlefield(player1, new Carnassid());
        harness.addToBattlefield(player2, new Heartstone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce a creature ability below one mana")
    void doesNotReduceCreatureAbilityBelowOneMana() {
        harness.addToBattlefield(player1, new SliverQueen());
        harness.addToBattlefield(player2, new Heartstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not reduce activated abilities of noncreatures")
    void doesNotReduceNoncreatureAbility() {
        harness.addToBattlefield(player1, new HornetCannon());
        harness.addToBattlefield(player2, new Heartstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
