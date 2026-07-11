package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VampiricFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Vampiric Feast deals 4 damage to target player and controller gains 4 life")
    void deals4DamageToPlayerAndGains4Life() {
        harness.setHand(player1, List.of(new VampiricFeast()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Vampiric Feast deals 4 damage to target creature, destroying a 3/3, and gains life")
    void deals4DamageToCreatureDestroysItAndGainsLife() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new VampiricFeast()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Vampiric Feast does not destroy a creature with toughness greater than 4")
    void doesNotDestroyToughCreature() {
        harness.addToBattlefield(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new VampiricFeast()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Avatar of Might"));
    }
}
