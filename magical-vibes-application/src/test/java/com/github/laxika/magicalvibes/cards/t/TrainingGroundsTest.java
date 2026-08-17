package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.e.EmberHauler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainingGroundsTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a creature's activated ability by two generic mana for its controller")
    void reducesOwnCreatureAbility() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        harness.addToBattlefield(player1, new AmaranthineWall());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce a creature's ability controlled by an opponent")
    void doesNotReduceOpponentCreatureAbility() {
        harness.addToBattlefield(player1, new AmaranthineWall());
        harness.addToBattlefield(player2, new TrainingGrounds());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not reduce an activated ability of a noncreature")
    void doesNotReduceNoncreatureAbility() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        harness.addToBattlefield(player1, new CircleOfProtectionRed());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not reduce a creature ability below one mana")
    void doesNotReduceBelowOneMana() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        harness.addToBattlefield(player1, new EmberHauler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
