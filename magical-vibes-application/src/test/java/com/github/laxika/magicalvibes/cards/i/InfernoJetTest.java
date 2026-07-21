package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InfernoJetTest extends BaseCardTest {

    private void addManaForInfernoJet() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Deals 6 damage to target opponent")
    void dealsDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new InfernoJet()));
        addManaForInfernoJet();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Cannot target yourself — only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new InfernoJet()));
        addManaForInfernoJet();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature — only opponent or planeswalker")
    void cannotTargetCreature() {
        Permanent creature = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new InfernoJet()));
        addManaForInfernoJet();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling {2} discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new InfernoJet()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Inferno Jet");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
