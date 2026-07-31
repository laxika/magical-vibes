package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZealotOfTheGodPharaohTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target opponent")
    void deals2DamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ability does not tap the creature")
    void doesNotTap() {
        Permanent zealot = new Permanent(new ZealotOfTheGodPharaoh());
        gd.playerBattlefields.get(player1.getId()).add(zealot);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(zealot.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target yourself — only an opponent")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target a creature — only opponent or planeswalker")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ZealotOfTheGodPharaoh());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
