package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcavationExplosionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a creature and creates a tapped Powerstone")
    void dealsDamageToCreatureAndCreatesPowerstone() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Air Elemental"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isEqualTo(3);
        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 3 damage to a player and creates a Powerstone")
    void dealsDamageToPlayerAndCreatesPowerstone() {
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }
}
