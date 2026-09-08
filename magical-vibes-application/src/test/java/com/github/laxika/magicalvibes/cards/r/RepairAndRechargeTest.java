package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.OblivionRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepairAndRechargeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an artifact and creates a tapped Powerstone")
    void returnsArtifactAndCreatesPowerstone() {
        Card artifact = new MindStone();
        castWithTarget(artifact);

        harness.assertOnBattlefield(player1, "Mind Stone");
        Permanent powerstone = findPermanents(player1, "Powerstone").getFirst();
        assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns an enchantment or planeswalker card")
    void returnsEnchantmentOrPlaneswalker() {
        Card enchantment = new OblivionRing();
        castWithTarget(enchantment);
        harness.assertOnBattlefield(player1, "Oblivion Ring");

        Card planeswalker = new JaceBeleren();
        harness.setGraveyard(player1, List.of(planeswalker));
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, planeswalker.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Jace Beleren");
    }

    @Test
    @DisplayName("Rejects a creature card as a target")
    void rejectsCreatureTarget() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithTarget(Card target) {
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
