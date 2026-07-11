package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurningFieldsTest extends BaseCardTest {

    private void addManaForBurningFields() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Deals 5 damage to target opponent")
    void dealsDamageToOpponent() {
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new BurningFields()));
        addManaForBurningFields();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Cannot target yourself — only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new BurningFields()));
        addManaForBurningFields();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature — only opponent or planeswalker")
    void cannotTargetCreature() {
        Permanent creature = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new BurningFields()));
        addManaForBurningFields();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
