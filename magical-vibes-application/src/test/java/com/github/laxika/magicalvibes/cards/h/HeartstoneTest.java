package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.e.EmberHauler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a creature's activated ability by one generic mana for any player")
    void reducesCreatureAbilityForAnyPlayer() {
        harness.addToBattlefield(player1, new AmaranthineWall());
        harness.addToBattlefield(player2, new Heartstone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce a one-mana creature ability below one mana")
    void doesNotReduceCreatureAbilityBelowOneMana() {
        harness.addToBattlefield(player1, new EmberHauler());
        harness.addToBattlefield(player2, new Heartstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not reduce activated abilities of noncreatures")
    void doesNotReduceNoncreatureAbility() {
        harness.addToBattlefield(player1, new CircleOfProtectionRed());
        harness.addToBattlefield(player2, new Heartstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
