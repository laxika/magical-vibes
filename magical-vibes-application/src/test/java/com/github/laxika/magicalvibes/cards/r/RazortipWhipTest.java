package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazortipWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to target opponent")
    void deals1DamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RazortipWhip());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Activating taps the artifact")
    void tapsAsCost() {
        Permanent whip = new Permanent(new RazortipWhip());
        gd.playerBattlefields.get(player1.getId()).add(whip);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(whip.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target yourself — only an opponent")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new RazortipWhip());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target a creature — only opponent or planeswalker")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new RazortipWhip());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new RazortipWhip());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent whip = new Permanent(new RazortipWhip());
        whip.tap();
        gd.playerBattlefields.get(player1.getId()).add(whip);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
