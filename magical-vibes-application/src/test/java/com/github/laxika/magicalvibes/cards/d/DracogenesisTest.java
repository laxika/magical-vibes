package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DracogenesisTest extends BaseCardTest {

    @Test
    @DisplayName("The controller can cast Dragon spells without paying their mana costs")
    void controllerCastsDragonForFree() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player1, List.of(new DragonWhelp()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting a Dragon for free does not spend mana")
    void freeDragonCastSpendsNoMana() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Dragon spells still require mana")
    void nonDragonSpellIsNotFree() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The opponent cannot use the controller's free Dragon casts")
    void opponentCannotCastDragonForFree() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player2, List.of(new DragonWhelp()));

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
